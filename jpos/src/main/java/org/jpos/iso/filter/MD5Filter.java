/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.iso.filter;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.*;
import org.jpos.util.LogEvent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.StringTokenizer;

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
public class MD5Filter implements ISOFilter, Configurable {
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
            for (int fld : f) {
                if (m.hasField(fld)) {
                    ISOComponent c = m.getComponent(fld);
                    if (c instanceof ISOBinaryField)
                        md.update((byte[]) c.getValue());
                    else
                        md.update(((String) c.getValue()).getBytes());
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
                            m.getValue(64), 0, rxDigest, 0, 8
                    );
                if (m.hasField (128))
                    System.arraycopy (
                            m.getValue(128), 0, rxDigest, 8, 8
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

