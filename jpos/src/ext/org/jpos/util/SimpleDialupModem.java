/*
 * $Log$
 * Revision 1.3  2000/03/14 00:00:12  apr
 * Added answer method
 *
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/11 01:25:01  apr
 * moved non ISO-8583 related classes from jpos.iso to jpos.util package
 * (AntiHog LeasedLineModem LogEvent LogListener LogProducer
 *  Loggeable Logger Modem RotateLogListener SimpleAntiHog SimpleDialupModem
 *  SimpleLogListener SimpleLogProducer SystemMonitor V24)
 *
 * Revision 1.1  1999/11/24 18:08:56  apr
 * Added VISA 1 Support
 *
 */

package org.jpos.util;

import java.io.*;
import javax.comm.*;

/**
 * Implements DialupModem
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class SimpleDialupModem implements Modem {
    V24 v24;
    String dialPrefix = "DT";
    String answerCommand = "ATA\r";
    public final String[] resultCodes = {
	"OK\r",
	"CONNECT\r",
	"RING\r",
	"NO CARRIER\r",
	"ERROR\r",
	"CONNECT",
	"NO DIALTONE\r",
	"BUSY\r",
	"NO ANSWER\r"
    };
    
    public SimpleDialupModem (V24 v24) {
	super();
	this.v24 = v24;
    }
    public void setDialPrefix (String dialPrefix) {
	this.dialPrefix = dialPrefix;
    }
    public void setAnswerCommand (String answerCommand) {
	this.answerCommand = answerCommand + "\r";
    }
    private boolean checkAT() throws IOException {
	return v24.waitfor ("AT\r", resultCodes, 10000) == 0;
    }
    private void reset() throws IOException {
	try {
	    v24.send ("AT\r"); Thread.sleep (250);
	    if (v24.waitfor ("ATE1Q0V1H0\r", resultCodes, 1000) == 0) 
		return;
	    v24.dtr (false);     Thread.sleep (1000);
	    v24.dtr (true);      Thread.sleep (1000);
	    v24.send ("+++"); Thread.sleep (1000);
	} catch (InterruptedException e) { 
	} 
	v24.flushAndLog();
	if (!checkAT())
	    throw new IOException ("Unable to reset");
    }
    public void dial (String phoneNumber, long aproxTimeout) 
	throws IOException
    {
	int rc;
	long expire = System.currentTimeMillis() + aproxTimeout;

	while (System.currentTimeMillis() < expire) {
	    try {
		reset();
		Thread.sleep (250);
		rc = v24.waitfor (
		    "ATB0M1X4L1"+dialPrefix+phoneNumber +"\r", 
			resultCodes,45000
		);
		if (rc == 5) {
		    Thread.sleep (500); // CD debouncing/settlement time
		    break;
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (InterruptedException e) { }
	}
    }
    public void hangup () throws IOException {
	reset();
	if (v24.isConnected())
	    throw new IOException ("Could not hangup");
    }
    public boolean isConnected() {
	return v24.isConnected();
    }
    public void answer () throws IOException {
	v24.setAutoFlushReceiver(false);
	if (!checkAT())
	    throw new IOException ("unable to initialize modem (0)");
	if (v24.waitfor ("ATV1Q0S0=0H0\r", resultCodes, 10000) != 0)
	    throw new IOException ("unable to initialize modem (1)");
	if (v24.waitfor ("RING", 10*60*1000) != 0)
	    throw new IOException ("10 minutes Idle - reinitializing");
	v24.flushReceiver();
	if (v24.waitfor (answerCommand, "CONNECT", 20*1000) != 0)
	    throw new IOException ("NO CARRIER Answering");
	v24.setAutoFlushReceiver(true);
    }
}
