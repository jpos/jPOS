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

package isoserver;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.channel.LoopbackChannel;
import org.jpos.iso.packager.ISO87APackager;

import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.ThreadPool;

public class Test implements ISORequestListener {

    public boolean process (ISOSource source, ISOMsg m) {
        try {
            m = (ISOMsg) m.clone ();
            m.setResponseMTI();
            m.set (new ISOField(39, "99"));
            source.send(m);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void main(String args[]) {
        try {
            Logger logger = new Logger();
            logger.addListener (new SimpleLogListener (System.out));

            ISOChannel clientSideChannel = new ASCIIChannel 
                (new ISO87APackager()); // see jpos.cfg

            ThreadPool pool = new ThreadPool (5, 30);
            pool.setLogger (logger, "iso-server-pool");

            ISOServer server = 
                new ISOServer (
                    8000, (ServerChannel) clientSideChannel, pool);

            server.setLogger (logger, "iso-server");
            server.addISORequestListener (new Test());
            new Thread (server).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
