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

package  securekeystore;

import  org.jpos.util.Logger;
import  org.jpos.util.LogSource;
import  org.jpos.util.SimpleLogListener;
import  org.jpos.security.SecureKeyStore;
import  org.jpos.security.SimpleKeyFile;
import  org.jpos.security.SecureKey;
import  org.jpos.security.SecureDESKey;
import  org.jpos.security.SMAdapter;
import  org.jpos.iso.ISOUtil;


/**
 * A class that demonstrates the use of the SecureKeyStore interface, using
 * SimpleKeyFile implementation.
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class Test {

    public static void main (String args[]) {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));
            // SimpleKeyFile creates "build/examples/securekeystore/keyfile"
            // automatically if it does not exist.
            SecureKeyStore ks = new SimpleKeyFile("build/examples/securekeystore/keyfile");
            if (ks instanceof LogSource)
                ((LogSource)ks).setLogger(logger, "ks");
            // Create a pin kek(key encrypting key)
            SecureDESKey pinKek = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZMK,
                    "CB3EC0FF092D3786167566882685307B", "5B3368");
            // store it in the key store
            ks.setKey("mastercard.pin-kek", pinKek);
            // Create a pin key
            SecureDESKey pinKey = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZPK,
                    "28E054ABD112F1BC", "7B414E");
            // store it in the key store
            ks.setKey("mastercard.pin-key", pinKey);
            pinKek = null;
            pinKey = null;
            // retrieve from key store
            SecureKey secureKey;
            secureKey = ks.getKey("mastercard.pin-kek");
            if (secureKey instanceof SecureDESKey)
                pinKek = (SecureDESKey) secureKey;
            secureKey = ks.getKey("mastercard.pin-key");
            if (secureKey instanceof SecureDESKey)
                pinKey = (SecureDESKey)secureKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
