/*
 * $Log$
 * Revision 1.2  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.1  2000/01/23 16:04:48  apr
 * Added OperatorLogListener
 *
 */

package org.jpos.util;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import org.jpos.core.Configuration;

/**
 * send e-mail with selected LogEvents to operator account
 * <b>Configuration properties</b>
 * <pre>
 *    jpos.operator.from=jpos
 *    jpos.operator.to=operator@foo.bar
 *    jpos.operator.subject.prefix=[jPOS]
 *    jpos.operator.tags="Operator ISORequest SystemMonitor"
 *    jpos.operator.delay=10000
 *    jpos.mail.smtp.host=localhost
 * </pre>
 *
 * @author apr@cs.com.uy
 * @version $Id$
 */
public class OperatorLogListener implements LogListener, Runnable {
    Configuration cfg;
    BlockingQueue queue;

    public OperatorLogListener (Configuration cfg) {
	super();
	this.cfg = cfg;
	queue = new BlockingQueue();
	new Thread(this).start();
    }
    public void run() {
	Thread.currentThread().setName ("OperatorLogListener");
	int delay = cfg.getInt ("jpos.operator.delay");
	try {
	    for (;;) {
		try {
		    if (delay > 0)
			Thread.sleep (delay);

		    LogEvent ev[] = new LogEvent[1];
		    if (queue.pending() > 0) {
			ev = new LogEvent [queue.pending()];
			for (int i=0; i < ev.length; i++)
			    ev[i] = (LogEvent) queue.dequeue();
		    } else 
			ev[0] = (LogEvent) queue.dequeue();
		    sendMail (ev);
		} catch (InterruptedException e) { }
	    }
	} catch (BlockingQueue.Closed e) { }
    }
    private void sendMail (LogEvent[] ev) {
	String from    = cfg.get ("jpos.operator.from", "jpos-logger");
	String to      = cfg.get ("jpos.operator.to");
	String subject = cfg.get ("jpos.operator.subject.prefix");
	if (ev.length > 1) 
	    subject = subject + " (multiple)";
	else
	    subject = subject + ev[0].getRealm() + " - " +ev[0].tag;

	// create some properties and get the default Session
	Properties props = System.getProperties();
	props.put("mail.smtp.host", cfg.get ("jpos.mail.smtp.host", 
		"localhost"));
	
	Session session = Session.getDefaultInstance(props, null);
	session.setDebug(false);
	
	try {
	    // create a message
	    MimeMessage msg = new MimeMessage(session);
	    msg.setFrom(new InternetAddress(from));
	    InternetAddress[] address = {new InternetAddress(to)};
	    msg.setRecipients(Message.RecipientType.TO, address);
	    msg.setSubject(subject);
	    Multipart mp = new MimeMultipart();

	    for(int i=0; i<ev.length; i++) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		PrintStream p = new PrintStream (buf);
		ev[i].dump (p, "");
		p.close();
	
		// create and fill the first message part
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setText(buf.toString());
		mbp.setFileName (ev[i].tag);
		mp.addBodyPart(mbp);
	    }
	    msg.setContent(mp);
	    msg.setSentDate(new Date());
	    Transport.send(msg);
	} catch (MessagingException mex) {
	    mex.printStackTrace();
	    Exception ex = null;
	    if ((ex = mex.getNextException()) != null) {
		ex.printStackTrace();
	    }
	}
    }
    private boolean checkOperatorTag(LogEvent ev) {
	String tags = cfg.get ("jpos.operator.tags");
	return tags.indexOf (ev.tag) >= 0;
    }
    public synchronized void log (LogEvent ev) {
	if (checkOperatorTag(ev))
	    queue.enqueue (ev);
    }
}
