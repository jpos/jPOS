package org.jpos.util;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.StringTokenizer;
import java.util.Vector;
import org.jpos.iso.ISOException;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;

/**
 * DirPoll operates on a set of directories
 * <ul>
 *  <li>request
 *  <li>response
 *  <li>tmp
 *  <li>run
 *  <li>bad
 * </ul>
 * scanning for incoming requests (of varying priorities)
 * on the request directory and processing them by means of
 * DirPoll.Processor.
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @since jPOS 1.2.7
 * @version $Revision$ $Date$
 */
public class DirPoll extends SimpleLogSource 
    implements Runnable, FilenameFilter, ReConfigurable
{
    private long pollInterval;
    private File requestDir;
    private File responseDir;
    private File tmpDir;
    private File badDir;
    private File runDir;
    private Vector prio;
    private int currentPriority;
    private String basePath;
    private ThreadPool pool;
    private Processor processor;
    private Configuration cfg;

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
    }
    public String getPath() {
        return basePath;
    }
    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }
    public long getPollInterval() {
        return pollInterval;
    }
    public void setProcessor (Processor processor) {
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
        return (new File(dir, name)).isFile();
    }

    //--------------------------------------------- Runnable implementation

    public void run() { 
	Thread.currentThread().setName ("DirPoll-"+basePath);
        if (prio.size() == 0)
            addPriority("");
        for (;;) {
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
		    Thread.sleep(pollInterval * 10);	// anti hog
		} catch (InterruptedException ex) { }
	    }
        }   
    }

    //----------------------------------------------------- public helpers

    public void createDirs() {
        requestDir.mkdirs();
        responseDir.mkdirs();
        tmpDir.mkdirs();
        badDir.mkdirs();
        runDir.mkdirs();
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

		byte[] resp = processor.process (
		    request.getName(), readRequest (request)
		);
		
		if (resp != null) 
		    writeResponse (request.getName(), resp);

		if (!request.delete ())
		    throw new DirPollException 
			("error: can't unlink request");
		
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
    public class DirPollException extends ISOException {
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
