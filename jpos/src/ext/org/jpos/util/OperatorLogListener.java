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
 * Revision 1.4  2000/11/02 12:09:17  apr
 * Added license to every source file
 *
 * Revision 1.3  2000/05/25 23:34:13  apr
 * Implements Configurable (used by QSP)
 *
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
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

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
public class OperatorLogListener 
    implements LogListener, Configurable, Runnable
{
    Configuration cfg;
    BlockingQueue queue;

    public OperatorLogListener () {
	super();
	queue = new BlockingQueue();
    }
    public OperatorLogListener (Configuration cfg) {
	super();
	this.cfg = cfg;
	queue = new BlockingQueue();
	new Thread(this).start();
    }
    public void setConfiguration (Configuration cfg) 
	throws ConfigurationException
    {
	this.cfg = cfg;
	assertProperty ("jpos.operator.to");
	assertProperty ("jpos.operator.subject.prefix");
	assertProperty ("jpos.operator.tags");
	assertProperty ("jpos.operator.delay");
	assertProperty ("jpos.mail.smtp.host");
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
    private void assertProperty (String propName) throws ConfigurationException
    {
	if (cfg.get (propName) == null)
	    throw new ConfigurationException 
		(propName + " property not present");
    }
}
