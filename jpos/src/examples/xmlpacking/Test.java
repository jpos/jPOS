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

/* $Id: */

package xmlpacking;

import java.util.Date;
import java.io.PrintStream;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.SimpleLogListener;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOException;
import org.jpos.iso.packager.XMLPackager;

public class Test extends SimpleLogSource {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void testXMLMessage () {
	LogEvent evt = new LogEvent (this, "XMLMessage");
	Date d = new Date();

	try {
	    ISOPackager packager = new XMLPackager(); 
	    packager.setLogger (getLogger(), "XMLPackager");

	    ISOMsg m = new ISOMsg();
	    m.setPackager (packager);
	    m.set (new ISOField (0,  "0800"));
	    m.set (new ISOField (3,  "000000"));
	    m.set (new ISOField (11, "000001"));
	    m.set (new ISOField (7,  ISODate.getDateTime(d)));
	    m.set (new ISOField (12, ISODate.getTime(d)));
	    m.set (new ISOField (13, ISODate.getDate(d)));
	    m.set 
		(new ISOField (48, "Less than '<' and greater than '>'"));
	    m.set (new ISOBinaryField (127, "BINARY FIELD".getBytes()));

	    // add inner message
	    ISOMsg inner = new ISOMsg (126);
	    inner.set (new ISOField (0,  "INNER-0"));
	    inner.set (new ISOField (1,  "INNER-1"));
	    inner.set (new ISOField (2,  "INNER-2"));
	    inner.set (new ISOBinaryField (3,  "INNER-BINARY".getBytes()));
	    m.set (inner);

	    byte[] b = m.pack();
	    evt.addMessage (m);

	    ISOMsg m1 = new ISOMsg();
	    m1.setPackager (packager);
	    m1.unpack (b);
	    evt.addMessage (m1);
	} catch (ISOException e) {
	    evt.addMessage (e);
	}
	Logger.log (evt);
    }
    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.testXMLMessage();
    }
}
