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

package org.jpos.iso.filter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;

/**
 * Computes an MD5 based Message Authentication Code
 * on outgoing messages and checks that MAC on incoming
 * ones.
 *
 * @author Alejandro P. Revilla
 * @version $Revision$ $Date$
 * @since 1.2.8
 * @see org.jpos.iso.ISOFilter
 */
public class MD5Filter implements ISOFilter, ReConfigurable {
    String key;
    int[] fields;

    public MD5Filter() {
        super();
    }
   /**
    * @param cfg
    * <ul>
    * <li>key    - initial key
    * <li>fields - Space separated field list
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException 
    {
        key = cfg.get ("key");
        String fieldList = cfg.get ("fields");
        if (fieldList == null)
            throw new ConfigurationException ("'fields' property not present");

        StringTokenizer st = new StringTokenizer (fieldList);
        int f[] = new int[st.countTokens()];

        for (int i=0; i<f.length; i++) 
            f[i] = Integer.parseInt (st.nextToken());

        fields = f;
    }
    public void setFields (int[] fields) {
        this.fields = fields;
    }
    /**
     * factory method
     * @param m current ISOMsg
     * @return key fields associated with this ISOMsg
     */
    public int[] getFields (ISOMsg m) {
        return fields;
    }
    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
        throws VetoException
    {
        if (key == null || fields == null)
            throw new VetoException ("MD5Filter not configured");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update (getKey());
            int[] f = getFields (m);
            for (int i=0; i<f.length; i++) {
                int fld = f[i];
                if (m.hasField (fld)) {
                    ISOComponent c = m.getComponent (fld);
                    if (c instanceof ISOBinaryField)
                        md.update ((byte[]) c.getValue());
                    else
                        md.update (((String)c.getValue()).getBytes());
                }
            }
            byte[] digest = md.digest();
            if (m.getDirection() == ISOMsg.OUTGOING) {
                m.set (new ISOBinaryField ( 64, digest, 0, 8));
                m.set (new ISOBinaryField (128, digest, 8, 8));
            } else {
                byte[] rxDigest = new byte[16];
                if (m.hasField (64))
                    System.arraycopy (
                        (byte[]) m.getValue(64), 0, rxDigest, 0, 8
                    );
                if (m.hasField (128))
                    System.arraycopy (
                        (byte[]) m.getValue(128), 0, rxDigest, 8, 8
                    );
                if (!Arrays.equals (digest, rxDigest)) {
                    evt.addMessage (m);
                    evt.addMessage ("MAC expected: "
                        +ISOUtil.hexString (digest));
                    evt.addMessage ("MAC received: "
                        +ISOUtil.hexString (rxDigest));
                    throw new VetoException ("invalid MAC");
                }
                m.unset  (64);
                m.unset (128);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new VetoException (e);
        } catch (ISOException e) {
            throw new VetoException (e);
        }
        return m;
    }
    /**
     * hook for custom key storage (i.e. crypto cards)
     */
    protected byte[] getKey() {
        return key.getBytes();
    }
}

