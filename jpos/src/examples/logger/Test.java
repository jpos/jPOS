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

package logger;

import java.io.PrintStream;
import org.jpos.iso.ISOException;
import org.jpos.util.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.SimpleLogSource;
import org.jpos.util.SimpleLogListener;

public class Test extends SimpleLogSource implements Loggeable {
    public Test (Logger logger, String realm) {
	setLogger (logger, realm);
    }
    public void testSimpleEvent() {
	LogEvent evt = new LogEvent (this, "SimpleEvent");
	evt.addMessage ("This is testSimpleEvent running");
	Logger.log (evt);
    }
    public void testLoggeable () {
	LogEvent evt = new LogEvent (this, "Loggeable");
	evt.addMessage (this);
	Logger.log (evt);
    }
    public void testException() {
	LogEvent evt = new LogEvent (this, "ExceptionDemo");
	evt.addMessage (
	    new Exception ("This is a sample exception - not an error")
	);
	Logger.log (evt);
    }
    public void testMulti() {
	LogEvent evt = new LogEvent (this, "MultipleMessages");
	evt.addMessage ("This is a multipart LogEvent");
	evt.addMessage ("We a this (Loggeable) and also an exception");
	evt.addMessage (this);
	evt.addMessage (
	    new Exception ("This is not an error either")
	);
	Logger.log (evt);
    }
    public void dump (PrintStream p, String indent) {
	String inner = indent + "  ";
	p.println (indent + "<loggeable>");
	p.println (inner  + "This class implements Loggeable");
	p.println (indent + "</loggeable>");
    }
    public void testISOException() {
	LogEvent evt = new LogEvent (this, "ExceptionDemo");
	evt.addMessage (
	    new Exception ("This is a simple exception - not an error")
	);
	evt.addMessage (
	    new ISOException ("This is a simple ISOException")
	);
	Exception simple    = new Exception ("Simple Exception");
	ISOException inner  = new ISOException ("some detail message", simple);
	ISOException outter  = new ISOException (inner);
	evt.addMessage (outter);
	Logger.log (evt);
    }

    public static void main (String args[]) {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	Test t = new Test (logger, "Test");
	t.testSimpleEvent();
	t.testLoggeable();
	t.testException();
	t.testMulti();
	t.testISOException();
    }
}
