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

package isomsg;

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
import org.jpos.iso.ISOException;

public class Test extends SimpleLogSource {
    int[] mask  = { 0, 3, 7 };
    int[] mask1 = { 0, 3, 7, 41, 42 };

    public Test (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    private ISOMsg createMessage(int[] mask) throws ISOException 
    {
        Date d = new Date();
        ISOMsg m = new ISOMsg("0800");
        m.set (new ISOField (3,  "000000"));
        m.set (new ISOField (7,  ISODate.getDateTime(d)));

        // alternate way of setting fields
        m.set (11, "000001");
        m.set (12, ISODate.getTime(d));
        m.set (13, ISODate.getDate(d));
        return mask == null ? m : (ISOMsg) m.clone(mask);
    }
    public void test() {
        LogEvent evt = new LogEvent (this, "Test");
        try {
            ISOMsg a = createMessage(null);
            evt.addMessage ("Original Message A (full):");
            evt.addMessage (a);

            ISOMsg b = (ISOMsg) a.clone();
            b.set (new ISOField (41, "12345678"));
            b.set (new ISOField (42, "123456789012345"));
            evt.addMessage 
                ("Message B is message A's clone plus fields 41 and 42");
            evt.addMessage (b);

            ISOMsg c = createMessage (mask);
            evt.addMessage ("Message C just fields 0, 3 and 7");
            evt.addMessage (c);

            ISOMsg d = (ISOMsg) c.clone();
            d.merge (b);
            evt.addMessage ("Message D is C merged with B");
            evt.addMessage (d);

            ISOMsg e = (ISOMsg) d.clone(mask1);
            evt.addMessage (
                "Message E == message (D and mask1) (clone(int[])");
            evt.addMessage (e);
        } catch (ISOException ex) {
            evt.addMessage (ex);
        }
        Logger.log (evt);
    }
    public void testNested() {
        LogEvent evt = new LogEvent (this, "TestNested");
        try {
            ISOMsg m      = createMessage(null);
            ISOMsg inner  = new ISOMsg (127); // goes at outter field 127
            inner.set (new ISOField (0,"001"));
            inner.set (new ISOField (2,"002"));
            inner.set (new ISOField (3,"003"));
            m.set (inner);
            evt.addMessage (m);
        } catch (ISOException ex) {
            evt.addMessage (ex);
        }
        Logger.log (evt);
    }
    public void testRenumber() {
        LogEvent evt = new LogEvent (this, "TestRenumber");
        try {
            ISOMsg m      = createMessage(null);
            ISOMsg inner  = new ISOMsg (127); // goes at outter field 127
            inner.set (new ISOField (0,"001"));
            inner.set (new ISOField (2,"002"));
            inner.set (new ISOField (3,"003"));
            m.set (inner);
            evt.addMessage (m);
            Logger.log (evt);

            m.move (127, 126);
            evt.addMessage ("Field 127 renumbered to 126");
        } catch (ISOException ex) {
            evt.addMessage (ex);
        }
        Logger.log (evt);
    }
    public static void main (String args[]) {
        Logger logger = new Logger();
        logger.addListener (new SimpleLogListener (System.out));

        Test t = new Test (logger, "Test");
        t.test();
        t.testNested();
        t.testRenumber();
    }
}
