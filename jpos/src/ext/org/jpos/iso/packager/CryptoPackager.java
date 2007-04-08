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

import java.io.InputStream;
import java.security.Key;

import javax.crypto.Cipher;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;

/**
 * packs/unpacks ISOMsgs into Encrypted representation
 *
 * @author bharavi gade
 * @version $Revision$ $Date$
 * @see ISOPackager
 * @see PackagerWrapper
 */
public class CryptoPackager extends PackagerWrapper
{
    private Cipher cipher=null;
    private Key key=null;
 
    public CryptoPackager() throws ISOException {
        super();
    }
    
    public void setCipher(Cipher cipher,Key key)
    {
        this.cipher=cipher;
        this.key=key;
    }
    
    public void setCipher(Cipher cipher)
    {
        this.cipher=cipher;
    }
    
    public void setKey(Key key)
    {
        this.key=key;
    }

    public  byte[] pack (ISOComponent c) throws ISOException
    {
        return encrypt(standardPackager.pack(c));
    }

    public int unpack (ISOComponent c, byte[] b) throws ISOException
    {
        return standardPackager.unpack(c,decrypt(b));
    }
    public void unpack (ISOComponent c, InputStream in) throws ISOException
    {
        throw new ISOException ("not implemented");
    }
    
    private byte[] encrypt(byte[] data) throws ISOException
    {
        try{
            cipher.init(Cipher.ENCRYPT_MODE,key);
            return cipher.doFinal(data);
        }
        catch(Exception e)
        {
            throw new ISOException(e);
        }
    }
    private byte[] decrypt(byte[] data) throws ISOException
    {
        try{
            cipher.init(Cipher.DECRYPT_MODE,key);
                return cipher.doFinal(data);
            }
        catch(Exception e)
        {
            throw new ISOException(e);
        }
    }
}

