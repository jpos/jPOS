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

/*
 * $Log$
 * Revision 1.8  2000/11/02 12:09:17  apr
 * Added license to every source file
 *
 * Revision 1.7  2000/04/26 12:33:09  apr
 * javadoc warnings ...
 *
 * Revision 1.6  2000/04/16 23:53:03  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.5  2000/03/20 19:24:13  apr
 * Testing ISOGetty ... minor bugfixes/timings in answer()/hangup()/reset()
 *
 * Revision 1.4  2000/03/14 00:01:12  apr
 * isConnected(): remove debugging code
 *
 * Revision 1.3  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.2  2000/01/17 18:26:06  apr
 * Supervise every 100 executions
 *
 * Revision 1.1  2000/01/11 01:25:02  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogSource
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogSource SystemMonitor V24)
 *
 * Revision 1.4  1999/12/17 14:58:30  apr
 * RXTX dataavailable workaround
 *
 * Revision 1.3  1999/12/15 16:07:36  apr
 * Protection against negative timeouts on readUntil
 *
 * Revision 1.2  1999/12/15 15:09:07  apr
 * debugging to readUntil ...
 *
 * Revision 1.1  1999/11/24 18:10:49  apr
 * VISA 1 support helper class
 *
 */

package org.jpos.util;

import java.io.*;
import java.util.*;
import javax.comm.*;
import org.jpos.iso.ISOUtil;

/**
 * handy Serial port functions 
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see <a href="http://www.frii.com/~jarvi/rxtx/">CommAPI</a>
 */
public class V24 implements SerialPortEventListener, LogSource 
{
    Logger logger;
    String realm;
    InputStream is;
    OutputStream os;
    SerialPort port;
    CommPortIdentifier portId;
    long lostCD;
    boolean watchCD, autoFlushRX;

    public static final int MAX_STRING_SIZE = 64*1024; 

    /**
     * @param portId
     * @param logger
     * @param realm
     * @throws IOException
     * @throws PortInUseException
     */
    public V24 (CommPortIdentifier portId, Logger logger, String realm) 
	throws IOException, PortInUseException
    {
	super();
	setLogger (logger, realm);
	this.portId = portId;
	this.autoFlushRX = false;
	initPort();
    }
    /**
     * @param portName (i.e. /dev/ttyS1)
     * @param logger
     * @param realm
     * @throws IOException
     * @throws PortInUseException
     */
    public V24 (String portName, Logger logger, String realm) 
	throws IOException, PortInUseException
    {
	super();
	setLogger (logger, realm);
        portId = getPortIdentifier(portName);
	initPort();
    }
    /**
     * removeEventListener, close Port
     */
    public void close() {
	port.removeEventListener();
	port.setDTR (false);
	port.close();
    }
    /**
     * port is opened by default within V24 constructors<br>
     * the close()/open() scheme is used to work around some
     * extrange behaveour on some CommAPI implementations
     */
    public void open() throws IOException, PortInUseException {
	initPort();
    }
    /**
     * @return already opened port
     */
    public SerialPort getSerialPort() {
	return port;
    }
    /**
     * @param watchCD true to monitor 
     */
    public void setWatchCD (boolean watchCD) {
	this.watchCD = watchCD;
    }
    /**
     * @return OutputStream associated with this port
     */
    public OutputStream getOutputStream() {
	return os;
    }
    /**
     * @return InputStream associated with this port
     */
    public InputStream getInputStream() {
	return is;
    }

    /**
     * @param ev SerialPortEvent
     * SerialPortEventListener requirements
     */
    public void serialEvent (SerialPortEvent ev) {
	int evType = ev.getEventType();
	switch (evType) {
	    case SerialPortEvent.DSR:
		if (!ev.getNewValue())
		    synchronized (this) {
			this.notify();
		    }
		break;
	    case SerialPortEvent.CD:
		checkCD (ev.getNewValue());
		if (!ev.getNewValue())
		    synchronized (this) {
			this.notify();
		    }
		break;
	    case SerialPortEvent.DATA_AVAILABLE:
		if (autoFlushRX)
		    try {
			flushAndLog();
		    } catch (IOException e) { }
		else {
		    synchronized (this) {
			this.notify();
		    }
		}
		break;
	}
	Thread.yield();
    }
    /**
     * @param baud Baud Rate
     * @param data number of Data bits
     * @param stop number of Stop bits
     * @param flow Flow Control mask
     * @see javax.comm.SerialPort
     * @throws UnsupportedCommOperationException
     */
    public synchronized void setSpeed
        (int baud, int data, int stop, int parity, int flow)
	throws UnsupportedCommOperationException
    {
	port.setSerialPortParams(baud, data, stop, parity);
	port.setFlowControlMode (flow);
    }

    /**
     * @param logger current logger
     * @param realm  logger realm
     * @see LogSource
     */
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    /**
     * @return current log realm
     * @see LogSource
     */
    public String getRealm () {
	return realm;
    }
    /**
     * @return current Logger
     */
    public Logger getLogger() {
	return logger;
    }

    private String elapsed (long start) {
	long el = System.currentTimeMillis() - start;
	if (el < 5000)
	    return el + "ms";
	else
	    return (el/1000) + "s";
    }
    private void checkCD(boolean on) {
	if (!on) {
	    lostCD = System.currentTimeMillis();
	    Logger.log (
		new LogEvent (this,"badnews",portId.getName()+" lost CD ")
	    );
	}
	else {
	    if (lostCD != 0) {
		Logger.log (
		    new LogEvent (this, "goodnews",
			portId.getName() 
			+ " recovered CD after "
			+ elapsed (lostCD))
		);
	    }
	    lostCD = 0;
	    synchronized (this) {
		this.notify();
	    }
	}
    }
    /**
     * @return connection status
     */
    public boolean isConnected() {
	return port.isDSR() && port.isCD();
	// return port.isDSR();
    }
    /**
     * flush receiver and dump discarded characters thru Logger
     * @throws IOException
     */
    public void flushAndLog () throws IOException {
	StringBuffer buf = new StringBuffer();
	while (is.available() > 0) {
	    while (is.available() > 0) {
		char c = (char) is.read();
		if (buf.length() < 1000) // paranoia check - ignore garbage
		    buf.append (c);
	    }
	    try {
		// avoid multiple log events. Wait for possibly
		// more characters to arrive
		Thread.sleep (250);
	    } catch (InterruptedException e) { }
	}
	Logger.log (
	    new LogEvent (this, "flush",
		ISOUtil.dumpString(
		    buf.toString().getBytes()
		)
	    )
	);
    }
    /**
     * flush receiver
     * @throws IOException
     */
    public void flushReceiver () throws IOException {
	while (is.available() > 0) 
	    is.read();
    }

    /**
     * @param b content to be sent (do not perform flush after sending)
     * @throws IOException
     */
    public synchronized void send (byte[] b) throws IOException {
	os.write (b);
	Logger.log (new LogEvent (this, "send", ISOUtil.dumpString(b)));
    }
    /**
     * @param b content to be sent (do not perform flush after sending)
     * @throws IOException
     */
    public synchronized void send (byte b) throws IOException {
	os.write (b);
	byte[] ab = new byte[1];
	ab[0] = b;
	Logger.log (new LogEvent (this, "send", ISOUtil.dumpString(ab)));
    }
    /**
     * @throws IOException
     */
    public void flushTransmitter () throws IOException {
	boolean retry = true;
	while (retry) {
	    try {
		os.flush ();
		retry = false;
	    } catch (IOException e) {
		Thread.yield();
		retry = true;
	    }
	}
    }
    /**
     * @param s content to be sent (performs flush after sending)
     * @throws IOException
     */
    public synchronized void send (String s) throws IOException {
	byte[] b = s.getBytes();
	send (b);
	flushTransmitter ();
	Logger.log (new LogEvent (this, "send-and-flush", ISOUtil.dumpString(b)));
    }
    /**
     * @param b buffer
     * @param timeout in milliseconds
     * @return number of characters actually read
     * @throws IOException
     */
    public int read (byte[] b, long timeout) throws IOException {
	int i;
	long max = System.currentTimeMillis() + timeout;
	for (i=0; i<b.length && System.currentTimeMillis() < max; ) {
	    synchronized (this) {
		if (is.available() > 0)
		    b[i++] = (byte) is.read();
		else {
		    long sleep = max - System.currentTimeMillis();
		    if (sleep > 0) {
			try {
			    wait (sleep);
			    if (!port.isDSR() || (watchCD && !port.isCD())) 
				throw new IOException ("DSR/CD off");
			} catch (InterruptedException e) { }
		    }
		}
	    }
	}
	return i;
    }
    /**
     * @param end pattern 
     * @param timeout in milliseconds
     * @param includeLast true if terminating char should be included
     * @return string including end character (may be 0 length)
     * @throws IOException
     */
    public String readUntil (String end, long timeout, boolean includeLast) 
	throws IOException 
    {
	LogEvent evt=new LogEvent (this, "readUntil",
	    ISOUtil.dumpString (end.getBytes()));
	StringBuffer buf = new StringBuffer();
	timeout = Math.abs (timeout);
	long max = System.currentTimeMillis() + timeout;
	setAutoFlushReceiver(false);
	for (;;) {
	    if (System.currentTimeMillis() > max) {
		evt.addMessage ("<timeout>"+timeout+"</timeout>");
		break;
	    }
	    synchronized (this) {
		if (is.available() > 0) {
		    char c = (char) is.read();
		    if (end.indexOf(c) >= 0) {
			if (includeLast)
			    buf.append (c);
			evt.addMessage ("<match/>");
			break;
		    }
		    if (buf.length() < MAX_STRING_SIZE) // paranoia check
			buf.append (c);
		}
		else {
		    long sleep = max - System.currentTimeMillis();
		    if (sleep > 0) {
			try {
			    wait (sleep);
			    if (!port.isDSR() || (watchCD && !port.isCD())) 
				throw new IOException ("DSR/CD off");
			} catch (InterruptedException e) { }
		    }
		}
	    }
	}
	String ret = buf.toString();
	evt.addMessage (
	    "<read>"+ISOUtil.dumpString(ret.getBytes())+"</read>"
	);
	
	Logger.log (evt);
	return ret;
    }
    /**
     * reads until newline or time expired
     * @param timeout in milliseconds
     * @return string (may be 0 length)
     * @throws IOException
     */
    public String readLine (long timeout) throws IOException {
	return readUntil ("\n", timeout, false);
    }

    /**
     * @param timeout in milliseconds
     * @param maxsize maximun size
     * @throws IOException
     */
    public String readString (long timeout, int maxsize) 
	throws IOException
    {
	StringBuffer buf = new StringBuffer();
	byte[] b = new byte[maxsize];
	int c = read (b, timeout);
	return new String (b, 0, c);
    }
    /**
     * @param pattern
     * @param timeout in milliseconds
     * @throws IOException
     */
    public int waitfor(String[] pattern, int timeout)
        throws IOException
    {
	long start = System.currentTimeMillis();
        int match = -1;
        byte[] buf = new byte[1];   
        StringBuffer readBuffer = new StringBuffer();
	LogEvent evt = new LogEvent (this, "waitfor");
	for (int i=0; i<pattern.length; i++) 
	    evt.addMessage (
		i +":" +ISOUtil.dumpString(pattern[i].getBytes())
	    );

        long expire = System.currentTimeMillis() + timeout;
        while (expire > System.currentTimeMillis()) {
	    readBuffer.append (
		readString (expire - System.currentTimeMillis(), 1)
	    );
   	    String s = readBuffer.toString();
	    for (int i=0; i<pattern.length; i++) {
   		if (s.indexOf(pattern[i]) >= 0) {
		    match = i;
   		    expire = 0;
		}
   	    }
        }
	evt.addMessage ("<buffer match=\"" + match + "\" elapsed=\""
	    + (System.currentTimeMillis() - start) + "\">"
	    +ISOUtil.dumpString (readBuffer.toString().getBytes())
	    +"</buffer>");
	Logger.log (evt);
        return match;
    }
    /**
     * @param pattern
     * @param timeout in milliseconds
     * @throws IOException
     */
    public int waitfor(String pattern, int timeout)
        throws IOException
    {
	String[] s = new String[1];
	s[0] = pattern;
	return waitfor (s, timeout);
    }
    /**
     * @param sendString
     * @param pattern
     * @param timeout in milliseconds
     * @throws IOException
     */
    public int waitfor(String sendString, String pattern, int timeout)
        throws IOException
    {
	send (sendString);
	String[] s = new String[1];
	s[0] = pattern;
	return waitfor (s, timeout);
    }
    /**
     * @param sendString
     * @param pattern
     * @param timeout in milliseconds
     * @throws IOException
     */
    public int waitfor(String sendString, String[] pattern, int timeout)
        throws IOException
    {
	send (sendString);
	return waitfor (pattern, timeout);
    }
    public synchronized void dtr (boolean value) throws IOException {
	Logger.log (new LogEvent (this, "dtr", value ? "on" : "off"));
	port.setDTR (value);
    }

    private CommPortIdentifier getPortIdentifier(String name)
        throws IOException
    {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
	LogEvent evt = new LogEvent (this, "getPortIdentifier");

        while (ports.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) ports.nextElement();
            if (id.getPortType()==CommPortIdentifier.PORT_SERIAL) {
                if(id.getName().equals(name)) {
		    evt.addMessage ("found:"+id.getName());
		    Logger.log (evt);
		    return id;
		}
		evt.addMessage ("  got:"+id.getName());
            }
        }
	IOException e = new IOException ("invalid port "+name);
	evt.addMessage (e);
	Logger.log (evt);
	throw e;
    }

    private void initPort () 
	throws IOException, PortInUseException
    {
	port   = (SerialPort) portId.open (realm, 2000);
	if (!port.isDSR() || !port.isCD())
	    lostCD = System.currentTimeMillis();
	try {
	    port.addEventListener (this);
	    port.notifyOnDSR(true);
	    port.notifyOnCarrierDetect(true);
	    port.notifyOnDataAvailable(true);
	} catch (TooManyListenersException e) {
	   Logger.log (new LogEvent (this, "initPort", e));
	}
	is = port.getInputStream();
	os = port.getOutputStream();
    }

    public static void main (String[] args) {
	Logger l = new Logger();
	l.addListener (new SimpleLogListener (System.out));
	V24 v24 = null;
	try {
	    v24 = new V24 ("/dev/ttyS0", l, "V24");
	    v24.setSpeed (2400,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE,
		0
                // SerialPort.FLOWCONTROL_RTSCTS_IN |
                //     SerialPort.FLOWCONTROL_RTSCTS_OUT
	    );
	    Thread.sleep (1000);
	    // v24.send ("AT\r");
	    // v24.waitfor ("OK", 60000);
	    new Thread() {
		public void run() {
		    for (int i=0;;i++) {
			System.out.println (i);
			Thread.yield();
		    }
		}
	    }.start();

	    Thread.sleep (120000);

	    // Modem mdm = new SimpleDialupModem (v24);
	    // mdm.dial ("000000", 60000);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	if (v24 != null)
	    v24.close();
    }
    public void setAutoFlushReceiver (boolean autoFlushRX) {
	if ( (this.autoFlushRX = autoFlushRX) )
	    try {
		flushReceiver();
	    } catch (IOException e) { }
    }
}
