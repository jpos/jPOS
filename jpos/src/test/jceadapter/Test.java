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

package  jceadapter;

import  junit.framework.*;
import  org.jpos.security.*;
import  org.jpos.security.jceadapter.*;
import  org.jpos.iso.ISOUtil;
import  java.util.Arrays;
import  java.io.File;


/**
 * <p>
 * Tests an SMAdapter implementation.
 * </p>
 * <p>
 * This current version, tests the JCESecurityModule using the:
 * "./src/ext-examples/smadapter/lmk" LMK file. If you changed the
 * LMK file, the tests would fail.
 * </p>
 * @todo implement tests for the rest of the methods in the SMAdapter
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class Test extends TestCase {
    SMAdapter sm = null;

    SecureDESKey tmk;
    byte[] tmkUnderZmk;
    SecureDESKey zmk;

    SecureDESKey zpk;
    byte[] tpkUnderTmk;
    SecureDESKey tpk;
    String pin;
    String accountNumber;
    EncryptedPIN pinUnderLmk;
    EncryptedPIN pinUnderTpk;
    EncryptedPIN pinUnderZpk;

    public Test (String s) {
        super(s);
        String lmk = "./src/ext-examples/smadapter/lmk";
        String providerClassName = "org.bouncycastle.jce.provider.BouncyCastleProvider";

        try {
            //sm = new JCESecurityModule(lmk, providerClassName);
            sm =  new JCESecurityModule(lmk); // uses SunJCE Provider

        } catch (SMException e) {
            System.err.println("Exception thrown:  " + e);
        }
    }

    /**
     * @todo the test data should be read from a file
     * Note that these test data are dependent on the LMK file, so if the
     * LMK file is changed, the tests will not succeed.
     */
    protected void setUp () {
        zmk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZMK,
                "CB3EC0FF092D3786167566882685307B", "5B3368");
        tmk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TMK,
                "A0DC2159505FE495FE59B4EAA63F89DF", "863605");
        tmkUnderZmk = ISOUtil.hex2byte("EF323279ABF56BF60BCA3FFD6E0C21EC");
        zpk = new SecureDESKey(SMAdapter.LENGTH_DES, SMAdapter.TYPE_ZPK,
                "28E054ABD112F1BC", "7B414E");
        tpkUnderTmk = ISOUtil.hex2byte("6FD58C73383E32E0914EF912736CD6BA");
        tpk = new SecureDESKey(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_TPK,
                "99C2B0C250C7190DC5B11D1B28F5C007", "01D863");
        pin ="92389";
        accountNumber = "400000123456";
        pinUnderLmk = new EncryptedPIN("7DA1C8E5DAA3E2F2", sm.FORMAT00,
                accountNumber);
        pinUnderTpk = new EncryptedPIN("C77BC10670AC51FE", sm.FORMAT01,
                accountNumber);
        pinUnderZpk = new EncryptedPIN("09D2CA9355DB1194", sm.FORMAT01,
                accountNumber);
    }

    protected void tearDown () {}

    public void testGenerateKey () {
        short keyLength = SMAdapter.LENGTH_DES3_2KEY;
        String keyType = SMAdapter.TYPE_CVK;
        try {
            SecureDESKey generatedKey = sm.generateKey(keyLength, keyType);
            assertEquals(generatedKey.getKeyLength(), keyLength);
            assertTrue(generatedKey.getKeyType().compareTo(keyType) == 0);
        } catch (Exception e) {
            //System.err.println("Exception thrown:  " + e);
            e.printStackTrace();
        }
    }

    public void testImportKey () {
        SecureDESKey tpk = null;
        try {
            tpk = sm.importKey(sm.LENGTH_DES3_2KEY, sm.TYPE_TPK, tpkUnderTmk, tmk, true);
            assertTrue(Arrays.equals(tpk.getKeyBytes(), this.tpk.getKeyBytes()));
            assertTrue(Arrays.equals(tpk.getKeyCheckValue(), this.tpk.getKeyCheckValue()));
        } catch (Exception e) {
            //System.err.println("Exception thrown:  " + e);
            e.printStackTrace();
        }
    }

    public void testExportKey() {
        byte[] tmkUnderZmk = null;
        try {
            tmkUnderZmk = sm.exportKey(tmk, zmk);
            assertTrue(Arrays.equals(tmkUnderZmk, this.tmkUnderZmk));
        } catch (Exception e) {
            //System.err.println("Exception thrown:  " + e);
            e.printStackTrace();
        }
    }

    public void testEncryptPIN() {
        EncryptedPIN pinUnderLmk = null;
        try {
            pinUnderLmk = sm.encryptPIN(pin, accountNumber);
            assertTrue(Arrays.equals(pinUnderLmk.getPINBlock(), this.pinUnderLmk.getPINBlock()));
        } catch (Exception e) {
            //System.err.println("Exception thrown:  " + e);
            e.printStackTrace();
        }
    }


    public void testDecryptPIN() {
        String pin = null;
        try {
            pin = sm.decryptPIN(pinUnderLmk);
            assertTrue(pin.compareTo(this.pin) == 0);
        } catch (Exception e) {
            //System.err.println("Exception thrown:  " + e);
            e.printStackTrace();
        }
    }


}



