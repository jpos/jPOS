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

package org.jpos.iso.packager;

import java.io.*;
import java.util.*;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.LogEvent;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.jpos.iso.*;

/*
 * $Log$
 * Revision 1.4  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.3  2000/05/03 16:15:37  apr
 * Added TYPE_BITMAP (future support of bitmap logging)
 *
 * Revision 1.2  2000/04/16 23:53:11  apr
 * LogProducer renamed to LogSource
 *
 * Revision 1.1  2000/04/16 22:12:33  apr
 * New packagers location org.jpos.iso.packager
 *
 * Revision 1.2  2000/04/08 23:55:12  apr
 * read sax.parser property
 *
 * Revision 1.1  2000/03/05 02:16:37  apr
 * Added XMLPackager
 *
 */

/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id$
 * @see ISOPackager
 */
public class XMLPackager extends HandlerBase
                         implements ISOPackager, LogSource
{
    protected Logger logger = null;
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;
    private Parser parser = null;
    private Stack stk;

    public static final String ISOMSG_TAG    = "isomsg";
    public static final String ISOFIELD_TAG  = "field";
    public static final String ID_ATTR       = "id";
    public static final String VALUE_ATTR    = "value";
    public static final String TYPE_ATTR     = "type";
    public static final String TYPE_BINARY   = "binary";
    public static final String TYPE_BITMAP   = "bitmap";

    public XMLPackager() throws ISOException {
	super();
	out = new ByteArrayOutputStream();
	p   = new PrintStream(out);
	stk = new Stack();
	try {
	    parser = ParserFactory.makeParser(
		System.getProperty(
		  "sax.parser", "org.apache.xerces.parsers.SAXParser"
		)
	    );
	    parser.setDocumentHandler (this);
	} catch (Exception e) {
	    throw new ISOException (e.toString());
	}
    }
    public byte[] pack (ISOComponent c) throws ISOException {
	LogEvent evt = new LogEvent (this, "pack");
	try {
	    if (!(c instanceof ISOMsg))
		throw new ISOException ("cannot pack "+c.getClass());
	    ISOMsg m = (ISOMsg) c;
	    byte[] b;
	    synchronized (this) {
		c.dump (p, "");
		b = out.toByteArray();
		out.reset();
	    }
	    if (logger != null)
		evt.addMessage (m);
	    return b;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} finally {
	    Logger.log(evt);
	}
    }

    public synchronized int unpack (ISOComponent c, byte[] b) 
	throws ISOException
    {
	LogEvent evt = new LogEvent (this, "unpack");
	try {
	    if (!(c instanceof ISOMsg))
		throw new ISOException 
		    ("Can't call packager on non Composite");

	    while (!stk.empty())    // purge from possible previous error
		stk.pop();

	    InputSource src = new InputSource (new ByteArrayInputStream(b));
	    parser.parse (src);
	    if (stk.empty())
		throw new ISOException ("error parsing");

	    ISOMsg m = (ISOMsg) c;
	    m.merge ((ISOMsg) stk.pop());

	    if (logger != null)	
		evt.addMessage (m);
	    return b.length;
	} catch (ISOException e) {
	    evt.addMessage (e);
	    throw e;
	} catch (IOException e) {
	    evt.addMessage (e);
	    throw new ISOException (e.toString());
	} catch (SAXException e) {
	    evt.addMessage (e);
	    throw new ISOException (e.toString());
	} finally {
	    Logger.log (evt);
	}
    }

    public void startElement (String name, AttributeList atts)
        throws SAXException
    {
	int fieldNumber = -1;
	try {
	    String id       = atts.getValue(ID_ATTR);
	    if (id != null) {
		try {
		    fieldNumber = Integer.parseInt (id);
		} catch (NumberFormatException ex) { }
	    }
	    if (name.equals (ISOMSG_TAG)) {
		if (fieldNumber >= 0) {
		    if (stk.empty())
			throw new SAXException ("inner without outter");

		    ISOMsg inner = new ISOMsg(fieldNumber);
		    ((ISOMsg)stk.peek()).set (inner);
		    stk.push (inner);
		} else {
		    stk.push (new ISOMsg(0));
		}
	    } else if (name.equals (ISOFIELD_TAG)) {
		ISOMsg m     = (ISOMsg) stk.peek();
		String value = atts.getValue(VALUE_ATTR);
		String type  = atts.getValue(TYPE_ATTR);
		if (id == null || value == null)
		    throw new SAXException ("invalid field");	
		if (TYPE_BINARY.equals (type)) {
		    m.set (new ISOBinaryField (
			fieldNumber, 
			    ISOUtil.hex2byte (
				value.getBytes(), 0, value.length()/2
			    )
			)
		    );
		}
		else
		    m.set (new ISOField (fieldNumber, value));
	    }
	} catch (ISOException e) {
	    throw new SAXException 
		("ISOException unpacking "+fieldNumber);
	}
    }
    public void endElement (String name) throws SAXException {
	if (name.equals (ISOMSG_TAG)) {
	    ISOMsg m = (ISOMsg) stk.pop();
	    if (stk.empty())
		stk.push (m); // push outter message
	}
    }

    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "<notavailable/>";
    }
    public void setLogger (Logger logger, String realm) {
	this.logger = logger;
	this.realm  = realm;
    }
    public String getRealm () {
	return realm;
    }
    public Logger getLogger() {
	return logger;
    }
}

