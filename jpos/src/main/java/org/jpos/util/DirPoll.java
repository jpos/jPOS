/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

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
    implements Runnable, FilenameFilter, Configurable, Destroyable
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

    private boolean shutdown;
    private boolean paused = false;
    private boolean shouldArchive;
    private boolean shouldTimestampArchive;
    private String archiveDateFormat;
    private boolean acceptZeroLength = false;

    public DirPoll () {
        prio = new Vector();
        setPollInterval(1000);
        setPath (".");
        pool = null;
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
    public void setAcceptZeroLength(boolean acceptZeroLength) {
        this.acceptZeroLength = acceptZeroLength;
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
     * Return instance implementing {@link FileProcessor} or {@link Processor}
     * @return
     * Object - need to be casted to {@link FileProcessor} or {@link Processor}
     */
    public Object getProcessor()
    {
    	return this.processor;
    }
    /**
     * DirPool receives Configuration requests
     * and pass along them to the underlying processor.
     * @param cfg Configuration object
     * @throws ConfigurationException on errors
     */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        if (cfg != null) {
            if (processor instanceof Configurable) {
                ((Configurable) processor).setConfiguration (cfg);
            }
            setRequestDir  (cfg.get ("request.dir",  "request"));
            setResponseDir (cfg.get ("response.dir", "response"));
            setTmpDir      (cfg.get ("tmp.dir",      "tmp"));
            setRunDir      (cfg.get ("run.dir",      "run"));
            setBadDir      (cfg.get ("bad.dir",      "bad"));
            setArchiveDir  (cfg.get ("archive.dir",  "archive"));
            setResponseSuffix (cfg.get ("response.suffix", null));
            setShouldArchive (cfg.getBoolean ("archive", false));
            setAcceptZeroLength (cfg.getBoolean ("zero-length", false));
            setArchiveDateFormat (
                cfg.get ("archive.dateformat", "yyyyMMddHHmmss")
            );
            setShouldTimestampArchive (cfg.getBoolean ("archive.timestamp", false));
        }
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
        if (v.isEmpty())
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
        boolean result;
        String ext = currentPriority >= 0 ? 
            ((String) prio.elementAt(currentPriority)) : null;
        if (ext != null && !name.endsWith(ext))
            return false;
        File f = new File (dir, name);
        if (acceptZeroLength){
             result = f.isFile();
        } else {
             result = f.isFile() && (f.length() > 0);
        }
        return result;
    }
    //--------------------------------------------- Runnable implementation

    public void run() { 
        Thread.currentThread().setName ("DirPoll-"+basePath);
        if (prio.isEmpty())
            addPriority("");
        while (!shutdown) {
            synchronized (this) {
                if (paused) {
                    try {
                        wait();
                        paused = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

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

    private void store(File f, File destinationDirectory) throws IOException {
        String storedFilename = f.getName();
        if (shouldTimestampArchive)
            storedFilename = f.getName() + "." + new SimpleDateFormat(archiveDateFormat).format(new Date());
        File destination = new File(destinationDirectory, storedFilename);
        if (!f.renameTo(destination))
            throw new IOException("Unable to archive " + "'" + f.getName() + "' in directory " + destinationDirectory);
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
         * @throws org.jpos.util.DirPoll.DirPollException on errors
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
                    DirPoll.this, "dirpoll", request.getName()
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
                    store(request, archiveDir);
                } else {
                    if (!request.delete ())
                        throw new DirPollException 
                            ("error: can't unlink request " + request.getName());                    
                }

            } catch (Throwable e) {
                logEvent = evt;
                evt.addMessage (e);
                try {
                    if ((e instanceof DirPollException && ((DirPollException)e).isRetry())) {
                        ISOUtil.sleep(pollInterval*10); // retry delay (pollInterval defaults to 100ms)
                        evt.addMessage("retrying");
                        store(request, requestDir);
                    } else {
                        store(request, badDir);
                    }
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
        boolean retry;
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
        public boolean isRetry() {
            return retry;
        }
        public void setRetry(boolean retry) {
            this.retry = retry;
        }
    }
    
    public void pause() {
        synchronized (this) {
            if (!paused) {
                paused = true;
                // Wake up the run() method from sleeping and tell it to pause
                notify();
            }
        }
    }

    public void unpause() {
        synchronized (this) {
            if (paused) {
                paused = false;
                // Wake up the wait()ing thread from being paused
                notify();
                // The run() method will reset the paused flag
            }
        }
    }
    
    public boolean isPaused() {
        synchronized (this) {
            return paused;
        }
    }
}
