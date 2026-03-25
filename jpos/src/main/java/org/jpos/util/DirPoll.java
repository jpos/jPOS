/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
@SuppressWarnings("unchecked")
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
    private ExecutorService executor;
    private Object processor;
    private final Object shutdownMonitor = new Object();

    private boolean shutdown;
    private boolean paused = false;
    private boolean shouldArchive;
    private boolean shouldCompressArchive;
    private boolean shouldTimestampArchive;
    private String archiveDateFormat;
    private boolean acceptZeroLength = false;
    private boolean regexPriorityMatching = false;
    private List<String> poolBatchFiles = new ArrayList<>();
    private static final long SHUTDOWN_WAIT = 15000;
    /** Configuration for this DirPoll instance. */
    protected Configuration cfg;

    /** Default constructor. */
    public DirPoll () {
        prio = new Vector();
        setPollInterval(1000);
        setPath (".");
        executor = null;
    }
    /**
     * Sets the base poll directory and derives the standard subdirectories from it.
     * @param base the base directory path
     */
    public synchronized void setPath(String base) {
        this.basePath = base;
        requestDir  = new File(base, "request");
        responseDir = new File(base, "response");
        tmpDir      = new File(base, "tmp");
        badDir      = new File(base, "bad");
        runDir      = new File(base, "run");
        archiveDir  = new File(base, "archive");
    }
    /**
     * When {@code true}, archive files are timestamped.
     * @param shouldTimestampArchive true to timestamp archived files
     */
    public void setShouldTimestampArchive(boolean shouldTimestampArchive) {
        this.shouldTimestampArchive = shouldTimestampArchive;
    }
    /**
     * Sets the date format pattern used when timestamping archived files.
     * @param dateFormat the date format pattern (see {@link java.text.SimpleDateFormat})
     */
    public void setArchiveDateFormat(String dateFormat) {
        this.archiveDateFormat = dateFormat;
    }
    /**
     * When {@code true}, processed request files are moved to the archive directory.
     * @param shouldArchive true to archive processed files
     */
    public void setShouldArchive(boolean shouldArchive) {
        this.shouldArchive = shouldArchive;
    }
    /**
     * When {@code true}, archived files are compressed.
     * @param shouldCompressArchive true to compress archived files
     */
    public void setShouldCompressArchive(boolean shouldCompressArchive) {
        this.shouldCompressArchive = shouldCompressArchive;
    }
    /**
     * When {@code true}, zero-length request files are accepted.
     * @param acceptZeroLength true to accept zero-length files
     */
    public void setAcceptZeroLength(boolean acceptZeroLength) {
        this.acceptZeroLength = acceptZeroLength;
    }
    /**
     * Returns the base poll directory path.
     * @return the base path
     */
    public String getPath() {
        return basePath;
    }
    /**
     * Sets the request sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setRequestDir (String dir) {
        requestDir = new File (basePath, dir);
    }
    /**
     * Sets the response sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setResponseDir (String dir) {
        responseDir = new File (basePath, dir);
    }
    /**
     * Sets the tmp sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setTmpDir (String dir) {
        tmpDir = new File (basePath, dir);
    }
    /**
     * Sets the bad (failed) sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setBadDir (String dir) {
        badDir = new File (basePath, dir);
    }
    /**
     * Sets the run (in-progress) sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setRunDir (String dir) {
        runDir = new File (basePath, dir);
    }
    /**
     * Sets the archive sub-directory name relative to the base path.
     * @param dir the sub-directory name
     */
    public void setArchiveDir (String dir) {
        archiveDir = new File (basePath, dir);
    }
    /**
     * Sets the polling interval in milliseconds.
     * @param pollInterval the interval in milliseconds
     */
    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }
    /**
     * Sets the suffix appended to response file names.
     * @param suffix the response file suffix
     */
    public void setResponseSuffix (String suffix) {
        this.responseSuffix = suffix;
    }
    /**
     * Returns the polling interval in milliseconds.
     * @return poll interval
     */
    public long getPollInterval() {
        return pollInterval;
    }
    /**
     * Sets the processor used to handle request files.
     * @param processor a {@link Processor} or {@link FileProcessor} instance
     */
    public void setProcessor (Object processor) {
        this.processor = processor;
    }

    /**
     * Returns the request directory.
     * @return request directory
     */
    protected File getRequestDir() {
        return requestDir;
    }

    /**
     * Returns the response directory.
     * @return response directory
     */
    protected File getResponseDir() {
        return responseDir;
    }

    /**
     * Returns the tmp directory.
     * @return tmp directory
     */
    protected File getTmpDir() {
        return tmpDir;
    }

    /**
     * Returns the bad (failed) directory.
     * @return bad directory
     */
    protected File getBadDir() {
        return badDir;
    }

    /**
     * Returns the run (in-progress) directory.
     * @return run directory
     */
    protected File getRunDir() {
        return runDir;
    }

    /**
     * Returns the archive directory.
     * @return archive directory
     */
    protected File getArchiveDir() {
        return archiveDir;
    }

    /**
     * Returns whether regex-based priority matching is enabled.
     * @return true if regex priority matching is active
     */
    public boolean isRegexPriorityMatching() {
        return regexPriorityMatching;
    }

    /**
     * Enables or disables regex-based file extension priority matching.
     * @param regexPriorityMatching true to enable regex priority matching
     */
    public void setRegexPriorityMatching(boolean regexPriorityMatching) {
        this.regexPriorityMatching = regexPriorityMatching;
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
    	this.cfg = cfg;
        if (cfg != null) {
            if (processor instanceof Configurable) {
                ((Configurable) processor).setConfiguration (cfg);
            }
            setRequestDir  (cfg.get ("request.dir",  "request"));
            setResponseDir(cfg.get("response.dir", "response"));
            setTmpDir(cfg.get("tmp.dir", "tmp"));
            setRunDir(cfg.get("run.dir", "run"));
            setBadDir(cfg.get("bad.dir", "bad"));
            setArchiveDir(cfg.get("archive.dir", "archive"));
            setResponseSuffix(cfg.get("response.suffix", null));
            setShouldArchive(cfg.getBoolean("archive", false));
            setShouldCompressArchive(cfg.getBoolean("archive.compress", false));
            setAcceptZeroLength (cfg.getBoolean ("zero-length", false));
            setArchiveDateFormat (
                cfg.get ("archive.dateformat", "yyyyMMddHHmmss")
            );
            setShouldTimestampArchive (cfg.getBoolean ("archive.timestamp", false));
            setRegexPriorityMatching(cfg.getBoolean("priority.regex", false));
        }
    }
    /**
     * Sets the file extension priority order for polling.
     * @param priorities blank-separated list of file extensions in priority order
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
    /**
     * Sets the thread pool used to execute processor tasks.
     * @param executor the executor service to use
     */
    public synchronized void setThreadPool (ExecutorService executor) {
        this.executor = executor;
    }
    
    //--------------------------------------- FilenameFilter implementation
    /**
     * {@link java.io.FilenameFilter} implementation that selects files matching the current priority extension.
     * @param dir the directory
     * @param name the file name
     * @return true if the file should be accepted
     */
    public boolean accept(File dir, String name) {
        boolean result;
        String ext = currentPriority >= 0 ?
                (String) prio.elementAt(currentPriority) : null;
        if (ext != null) {
            if (isRegexPriorityMatching()) {
                if (!name.matches(ext))
                    return false;
            } else {
                if (!name.endsWith(ext))
                    return false;
            }
        }
        File f = new File (dir, name);
        if (acceptZeroLength){
             result = f.isFile();
        } else {
             result = f.isFile() && f.length() > 0;
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
                    executor.submit(new ProcessorRunner (f));
                    Thread.yield(); // be nice
                }
                else {
                    synchronized (shutdownMonitor) {
                        if (!shutdown && pollInterval > 0L) {
                            shutdownMonitor.wait(pollInterval);
                        }
                    }
                }
            } catch (InterruptedException e) {
            } catch (Throwable e) {
                Logger.log (new LogEvent (this, "dirpoll", e));
                try {
                    synchronized (shutdownMonitor) {
                        if (!shutdown && pollInterval > 0L) {
                            shutdownMonitor.wait(pollInterval * 10);
                        }
                    }
                } catch (InterruptedException ex) { }
            }
        }
    }
    public void destroy () {
        synchronized (shutdownMonitor) {
            shutdown = true;
            shutdownMonitor.notifyAll();

            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(SHUTDOWN_WAIT, TimeUnit.MILLISECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    //----------------------------------------------------- public helpers

    /** Creates all required poll directories (request, response, tmp, bad, run, archive). */
    public void createDirs() {
        requestDir.mkdirs();
        responseDir.mkdirs();
        tmpDir.mkdirs();
        badDir.mkdirs();
        runDir.mkdirs();
        archiveDir.mkdirs();
    }
    /**
     * Adds a file extension to the priority list.
     * @param fileExtension the extension to add (e.g. {@code "xml"})
     */
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
            throw new IOException("Unable to move "+f.getName());
        return destination;
    }

    private File store(File f, File destinationDirectory) throws IOException {
        String storedFilename = f.getName();
        if (shouldTimestampArchive)
            storedFilename = f.getName() + "." + new SimpleDateFormat(archiveDateFormat).format(new Date());
        File destination = new File(destinationDirectory, storedFilename);
        if (!f.renameTo(destination))
            throw new IOException("Unable to archive " + "'" + f.getName() + "' in directory " + destinationDirectory);
        return destination;
    }

    private void compress(File f) throws IOException {
        ZipUtil.zipFile(f, new File(f.getAbsolutePath() + ".zip"));
        f.delete();
    }

    /**
     * Scans the request directory for the next file to process, respecting priority order.
     * @return the next request {@link File}, or {@code null} if none is available
     */
    protected File scan() {
        if (prio.size() > 1) {
            for (currentPriority = 0; currentPriority < prio.size(); currentPriority++) {
                if (poolBatchFiles.isEmpty()) {
                    String[] files = requestDir.list(this);
                    if (files != null && files.length > 0) {
                        poolBatchFiles = new ArrayList(Arrays.asList(files));
                        return new File(requestDir, poolBatchFiles.remove(0));
                    }
                } else {
                    return new File(requestDir, poolBatchFiles.remove(0));
                }
            }
        } else {
            if (poolBatchFiles.isEmpty()) {
                String[] files = requestDir.list();
                if (files != null && files.length > 0) {
                    poolBatchFiles = new ArrayList(Arrays.asList(files));
                    return new File(requestDir, poolBatchFiles.remove(0));
                }
            } else {
                return new File(requestDir, poolBatchFiles.remove(0));
            }
        }
        return null;
    }

    private synchronized ExecutorService getExecutor() {
        if (executor == null) {
        	if (cfg.getBoolean("virtual-threads", true)) {
        		 executor = Executors.newFixedThreadPool(10, Thread.ofVirtual().factory());
        	} else {
        		executor = Executors.newFixedThreadPool(10, Thread.ofPlatform().inheritInheritableThreadLocals(true).factory());
        	}
        }
        return executor;
    }

    // ------------------------------------------------ inner interfaces
    /** Callback interface for processing binary request files. */
    public interface Processor {
        /**
         * Processes a request and returns a response.
         * @param name request file name
         * @param request request image bytes
         * @return response bytes (or null if none)
         * @throws DirPollException on processing errors
         */
        byte[] process(String name, byte[] request)
            throws DirPollException;
    }
    /** Callback interface for processing request {@link File} objects directly. */
    public interface FileProcessor {
        /**
         * Processes a request file.
         * @param name the request file
         * @throws DirPollException on errors
         */
        void process(File name) throws DirPollException;
    }
    /** Runnable that moves a request to the run directory and dispatches it to the processor. */
    public class ProcessorRunner implements Runnable {
        File request;
        LogEvent logEvent;
        /**
         * Creates a ProcessorRunner for the given request file.
         * @param request the request file
         * @throws IOException on I/O failure while moving the file
         */
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
                    File archivedFile = store(request, archiveDir);
                    if (shouldCompressArchive) {
                        compress(archivedFile);
                    }
                } else {
                    if (!request.delete ())
                        throw new DirPollException 
                            ("error: can't unlink request " + request.getName());                    
                }

            } catch (Throwable e) {
                logEvent = evt;
                evt.addMessage (e);
                try {
                    if (e instanceof DirPollException && ((DirPollException)e).isRetry()) {
                        synchronized (shutdownMonitor) {
                            if (!shutdown) {
                                try {
                                    if (pollInterval > 0L)
                                        shutdownMonitor.wait(pollInterval * 10); // retry delay (pollInterval defaults to 100ms)
                                } catch (InterruptedException ie) {
                                }
                            }
                        }
                        evt.addMessage("retrying");
                        moveTo(request, requestDir);
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
    /** Exception thrown by {@link Processor} or {@link FileProcessor} to signal a processing error. */
    public static class DirPollException extends ISOException {
        /** When {@code true}, the request should be retried rather than moved to bad. */
        boolean retry;
        /** Default constructor. */
        public DirPollException () {
            super();
        }
        /**
         * Constructs a DirPollException with the given message.
         * @param detail the error message
         */
        public DirPollException (String detail) {
            super(detail);
        }
        /**
         * Constructs a DirPollException wrapping the given exception.
         * @param nested the nested exception
         */
        public DirPollException (Exception nested) {
            super(nested);
        }
        /**
         * Constructs a DirPollException with a message and nested exception.
         * @param detail the error message
         * @param nested the nested exception
         */
        public DirPollException (String detail, Exception nested) {
            super(detail, nested);
        }
        /**
         * Returns whether the failed request should be retried.
         * @return true if retry is requested
         */
        public boolean isRetry() {
            return retry;
        }
        /**
         * Sets whether the failed request should be retried.
         * @param retry true to retry, false to move to bad directory
         */
        public void setRetry(boolean retry) {
            this.retry = retry;
        }
    }

    /** Pauses the poll loop. */
    public void pause() {
        synchronized (this) {
            if (!paused) {
                paused = true;
                // Wake up the run() method from sleeping and tell it to pause
                notify();
            }
        }
    }

    /** Resumes a paused poll loop. */
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
    
    /**
     * Returns whether the poll loop is currently paused.
     * @return true if paused
     */
    public boolean isPaused() {
        synchronized (this) {
            return paused;
        }
    }
}
