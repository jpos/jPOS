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

package externalizable;

import java.io.*;
import org.jpos.iso.*;
import org.jpos.util.*;
import org.jpos.iso.packager.*;

public class Test extends SimpleLogSource {
    public Test (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    public ISOMsg readMessage() throws ISOException, IOException {
        FileInputStream fis = new FileInputStream (
            "src/examples/externalizable/isomsg.xml"
        );
        byte[] b = new byte[fis.available()];
        fis.read (b);
        ISOMsg m = new ISOMsg ();
        m.setPackager (new XMLPackager());
        m.unpack (b);
        m.setHeader ("ISO".getBytes());
        m.setDirection (ISOMsg.INCOMING);
        return m;
    }
    public byte[] getImage (ISOMsg m) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream (b);
        o.writeObject (m);
        o.flush();
        return b.toByteArray();
    }
    public ISOMsg getISOMsg (byte[] buf) 
        throws IOException, ClassCastException, ClassNotFoundException
    {
        ByteArrayInputStream b = new ByteArrayInputStream(buf);
        ObjectInputStream o = new ObjectInputStream (b);
        return (ISOMsg) o.readObject();
    }
    public void test() throws Exception {
        Profiler prof = new Profiler();
        LogEvent evt  = new LogEvent (this, "test");
        ISOMsg m = readMessage();
        prof.checkPoint ("readMessage");
        byte[] b = getImage (m);
        prof.checkPoint ("getImage");
        for (int i=0; i<1000; i++)
            getImage(m);
        prof.checkPoint ("1000 getImages");
        ISOMsg m1 = getISOMsg (b);
        prof.checkPoint ("getISOMsg");
        for (int i=0; i<1000; i++)
            getISOMsg(b);
        prof.checkPoint ("1000 getISOMsg");

        evt.addMessage (m);
        evt.addMessage (ISOUtil.hexString (b));
        evt.addMessage ("image length: "+b.length);
        evt.addMessage (m1);
        evt.addMessage (prof);
        Logger.log (evt);
    }
    public static void main (String args[]) {
        Logger logger = new Logger();
        logger.addListener (new SimpleLogListener (System.out));

        Test t = new Test (logger, "Test");
        try {
            t.test();
        } catch (Exception e) {
            Logger.log (new LogEvent (t, "main", e));
        }
    }
}
