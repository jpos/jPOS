/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jpos.core.*;
import org.jpos.iso.ISOException;
import org.jpos.util.*;

/**
 * DirPoll operates on a set of directories which defaults to
 * <ul>
 *  <li>request
 *  <li>response
 *  <li>tmp
 *  <li>run
 *  <li>bad
 *  <li>archive
 * </ul>
 * scanning for incoming requests (of varying priorities)
 * on the request directory and processing them by means of
 * DirPoll.Processor or DirPoll.FileProcessor
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @author <a href="mailto:mmilliss@moneyswitch.net">Matthew Milliss</a>
 * @since jPOS 1.2.7
 * @version $Revision$ $Date$
 */
public class DirPoll extends SimpleLogSource 
    implements Runnable, FilenameFilter, ReConfigurable, Destroyable
{
    private long pollInterval;
    private File requestDir;
    private File responseDir;
    private File tmpDir;
    private File badDir;
    private File runDir;
    private File archiveDir;
    private Vector prio;
    private int currentPriority;
    private String basePath;
    private String responseSuffix;
    private ThreadPool pool;
    private Object processor;
    private Configuration cfg;
    private boolean shutdown;
    private boolean shouldArchive;
    private boolean shouldTimestampArchive;
    private String archiveDateFormat;
    

    //------------------------------------ Constructor/setters/getters, etc.
    /**
     * @param basePath base path of DirPoll tree
     * @param pool ThreadPoll (may be null)
     */
    public DirPoll () {
        prio = new Vector();
        setPollInterval(1000);
        setPath (".");
        pool = null;
        cfg = null;
    }
    public synchronized void setPath(String base) {
        this.basePath = base;
        requestDir  = new File(base, "request");
        responseDir = new File(base, "response");
        tmpDir      = new File(base, "tmp");
        badDir      = new File(base, "bad");
        runDir      = new File(base, "run");
        archiveDir  = new File(base, "archive");
    }
    public void setShouldTimestampArchive(boolean shouldTimestampArchive) {
        this.shouldTimestampArchive = shouldTimestampArchive;
    }
    public void setArchiveDateFormat(String dateFormat) {
        this.archiveDateFormat = dateFormat;
    }
    public void setShouldArchive(boolean shouldArchive) {
        this.shouldArchive = shouldArchive;
    }
    public String getPath() {
        return basePath;
    }
    public void setRequestDir (String dir) {
        requestDir = new File (basePath, dir);
    }
    public void setResponseDir (String dir) {
        responseDir = new File (basePath, dir);
    }
    public void setTmpDir (String dir) {
        tmpDir = new File (basePath, dir);
    }
    public void setBadDir (String dir) {
        badDir = new File (basePath, dir);
    }
    public void setRunDir (String dir) {
        runDir = new File (basePath, dir);
    }
    public void setArchiveDir (String dir) {
        archiveDir = new File (basePath, dir);
    }
    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }
    public void setResponseSuffix (String suffix) {
        this.responseSuffix = suffix;
    }
    public long getPollInterval() {
        return pollInterval;
    }
    public void setProcessor (Object processor) {
        this.processor = processor;
    }
    /**
     * DirPool is not really Configurable, it uses QSPConfig instead
     * but anyway it receives Configuration and ReConfiguration requests
     * and pass along them to the underlying processor.
     * @param cfg Configuration object 
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        if (processor != null) {
            if ( (processor instanceof ReConfigurable) ||
                 ((cfg == null) && (processor instanceof Configurable)) )
            {
                ((Configurable) processor).setConfiguration (cfg);
            }
        }
        this.cfg = cfg;

        setRequestDir  (cfg.get ("request.dir",  "request"));
        setResponseDir (cfg.get ("response.dir", "response"));
        setTmpDir      (cfg.get ("tmp.dir",      "tmp"));
        setRunDir      (cfg.get ("run.dir",      "run"));
        setBadDir      (cfg.get ("bad.dir",      "bad"));
        setArchiveDir  (cfg.get ("archive.dir",  "archive"));
        setResponseSuffix (cfg.get ("response.suffix", null));
        setShouldArchive (cfg.getBoolean ("archive", false));
        setArchiveDateFormat (
            cfg.get ("archive.dateformat", "yyyyMMddHHmmss")
        );
        setShouldTimestampArchive (cfg.getBoolean ("archive.timestamp", false));
    }
    /**
     * @param priorities blank separated list of extensions
     */
    public void setPriorities (String priorities) {
        StringTokenizer st = new StringTokenizer (priorities);
        Vector v = new Vector();
        while (st.hasMoreTokens()) {
            String ext = st.nextToken();
            v.addElement (ext.equals ("*") ? "" : ext);
        }
        if (v.size() == 0)
            v.addElement ("");
        synchronized (this) {
            prio = v;
        }
    }
    public synchronized void setThreadPool (ThreadPool pool) {
        this.pool = pool;
    }
    //--------------------------------------- FilenameFilter implementation
    public boolean accept(File dir, String name) {
        String ext = currentPriority >= 0 ? 
            ((String) prio.elementAt(currentPriority)) : null;
        if (ext != null && !name.endsWith(ext))
            return false;
        File f = new File (dir, name);
        return f.isFile() && (f.length() > 0);
    }

    //--------------------------------------------- Runnable implementation

    public void run() { 
        Thread.currentThread().setName ("DirPoll-"+basePath);
        if (prio.size() == 0)
            addPriority("");
        while (!shutdown) {
            try {
                File f;
                synchronized (this) {
                    f = scan();
                }
                if (f != null) {
                    getPool().execute (new ProcessorRunner (f));
                    Thread.yield(); // be nice
                }
                else
                    Thread.sleep(pollInterval);
            } catch (InterruptedException e) { 
            } catch (Throwable e) {
                Logger.log (new LogEvent (this, "dirpoll", e));
                try {
                    Thread.sleep(pollInterval * 10);    // anti hog
                } catch (InterruptedException ex) { }
            }
        }   
    }
    public void destroy () {
        shutdown = true;
    }

    //----------------------------------------------------- public helpers

    public void createDirs() {
        requestDir.mkdirs();
        responseDir.mkdirs();
        tmpDir.mkdirs();
        badDir.mkdirs();
        runDir.mkdirs();
        archiveDir.mkdirs();
    }
    public void addPriority(String fileExtension) {
        prio.addElement (fileExtension);
    }

    //----------------------------------------------------- private helpers
    private byte[] readRequest (File f) throws IOException {
        byte[] b = new byte[(int) f.length()];
        FileInputStream in = new FileInputStream(f);
        in.read(b);
        in.close();
        return b;
    }
    private void writeResponse (String requestName, byte[] b) 
        throws IOException
    {
        if (responseSuffix != null) {
            int pos = requestName.lastIndexOf ('.');
            if (pos > 0)
                requestName = requestName.substring (0, pos) + responseSuffix;
        }

        File tmp = new File(tmpDir, requestName);
        FileOutputStream out = new FileOutputStream(tmp);
        out.write(b);
        out.close();
        moveTo (tmp, responseDir);
    }

    private File moveTo(File f, File dir) throws IOException {
        File destination = new File(dir, f.getName());
        if (!f.renameTo(destination))
            throw new IOException("Unable to move"+f.getName());
        return destination;
    }

    private void archive(File f) throws IOException {
        String archiveFilename = f.getName();
        if (shouldTimestampArchive)
            archiveFilename = f.getName() + "." + new SimpleDateFormat(archiveDateFormat).format(new Date());
        File destination = new File(archiveDir, archiveFilename);
        if (!f.renameTo(destination))
            throw new IOException("Unable to archive " + "'" + f.getName() + "'");
    }
    
    private File scan() {
        for (currentPriority=0; 
            currentPriority < prio.size(); currentPriority++)
        {
            String files[] = requestDir.list(this);
            if (files != null && files.length > 0)
                return new File(requestDir, files[0]);
        }
        return null;
    }

    private synchronized ThreadPool getPool() {
        if (pool == null)
            pool = new ThreadPool (1, 10);
        return pool;
    }

    // ------------------------------------------------ inner interfaces
    public interface Processor {
        /**
         * @param name request name
         * @param request request image
         * @return response (or null)
         */
        public byte[] process(String name, byte[] request) 
            throws DirPollException;
    }
    public interface FileProcessor {
        /**
         * @param name request File
         * @exception should something go wrong
         */
        public void process (File name) throws DirPollException;
    }
    public class ProcessorRunner implements Runnable {
        File request;
        LogEvent logEvent;
        public ProcessorRunner (File request) throws IOException {
            this.request = moveTo (request, runDir);
            this.logEvent = null;
        }
        public void run() {
            LogEvent evt = 
                new LogEvent (
                    (LogSource) DirPoll.this, "dirpoll", request.getName()
                );
            try {
                if (processor == null) 
                    throw new DirPollException 
                        ("null processor - nothing to do");
                else if (processor instanceof Processor) {
                    byte[] resp = ((Processor) processor).process (
                        request.getName(), readRequest (request)
                    );
                    if (resp != null) 
                        writeResponse (request.getName(), resp);
                } else if (processor instanceof FileProcessor) 
                    ((FileProcessor) processor).process (request);

                if (shouldArchive) {
                    archive(request);
                } else {
                    if (!request.delete ())
                        throw new DirPollException 
                            ("error: can't unlink request " + request.getName());                    
                }

            } catch (Throwable e) {
                logEvent = evt;
                evt.addMessage (e);
                try {
                    moveTo (request, badDir);
                } catch (IOException _e) {
                    evt.addMessage ("Can't move to "+badDir.getPath());
                    evt.addMessage (_e);
                }
            } finally {
                if (logEvent != null) 
                    Logger.log (logEvent);
            }
        }
    }
    public static class DirPollException extends ISOException {
        public DirPollException () {
            super();
        }
        public DirPollException (String detail) {
            super(detail);
        }
        public DirPollException (Exception nested) {
            super(nested);
        }
        public DirPollException (String detail, Exception nested) {
            super(detail, nested);
        }
    }
}
