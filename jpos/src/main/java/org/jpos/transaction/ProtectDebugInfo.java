/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.transaction;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.jpos.util.FSDMsg;
import org.jpos.util.ProtectedLogListener;

import java.io.Serializable;
import java.util.Arrays;

/**
 *Sample Usage:
 *<pre>
 *    &lt;participant class="org.jpos.transaction.ProtectDebugInfo"&gt;
 *
 *        &lt;property name="protect-entry" value="REQUEST" /&gt;
 *        &lt;property name="protect-entry" value="RESPONSE" /&gt;
 *        &lt;property name="protect-entry" value="PAN, EXP, REQUEST_ICC_DATA" /&gt;
 *        &lt;property name="wipe-entry"    value="EXPDATE" /&gt;
 *
 *        &lt;!-- if the protected ctx entry is an ISOMsg --&gt;
 *        &lt;property name="protect-ISOMsg" value="2" /&gt;
 *        &lt;property name="protect-ISOMsg" value="35, 45" /&gt;
 *        &lt;property name="wipe-ISOMsg"    value="52, 55" /&gt;
 *
 *        &lt;!-- if the protected ctx entry is a TLVList --&gt;
 *        &lt;property name="wipe-TLVList" value="0x56" /&gt;
 *        &lt;property name="wipe-TLVList" value="0x57" /&gt;
 *        &lt;property name="wipe-TLVList" value="0x5a, 0x5f20" /&gt;
 *
 *        &lt;!-- if the protected ctx entry is a FSDMsg --&gt;
 *        &lt;property name="protect-FSDMsg" value="account-number" /&gt;
 *        &lt;property name="protect-FSDMsg" value="track2-data" /&gt;
 *        &lt;property name="wipe-FSDMsg"    value="secret-key" /&gt;
 *
 *    &lt;/participant&gt;
 *</pre>
 *
 * Configuration properties accept comma/space-separated values, but can also be given in multiple occurrences.
 * All occurrences of the same property will be merged into a single list.
 * <p>
 * @author Alejandro Revilla
 * @author David Bergert
 * @author Barzilai Spinak
 * </p>
 **/

public class ProtectDebugInfo implements AbortParticipant, Configurable {
     private static final String COMMA_SPACE_SEPARATOR = "[,\\s]+";
     private String[] protectedEntries;
     private String[] wipedEntries;
     private String[] protectFSD;
     private String[] protectISO;
     private String[] wipeISO;
     private String[] wipeFSD;
     private String[] wipeTLV;

     public int prepare (long id, Serializable o) {
         return PREPARED | READONLY;
     }
     public int prepareForAbort (long id, Serializable o) {
         return PREPARED | READONLY;
     }
     public void commit (long id, Serializable o) {
         protect ((Context) o);
     }
     public void abort  (long id, Serializable o) {
         protect ((Context) o);
     }

     private void protect (Context ctx) {
         /* wipe by removing entries from the context  */
         for (String s: wipedEntries)
             ctx.remove(s);

         /* Protect entry items */
         for (String s: protectedEntries) {
             Object o = ctx.get (s);

             if (o instanceof ISOMsg) {
                 ISOMsg m = ctx.get (s);
                 if (m != null) {
                     m = (ISOMsg) m.clone();
                     ctx.put (s, m);   // place a clone in the context
                     for (String p: protectISO)
                         protectField(m,p);
                     for (String p: wipeISO)
                         wipeField(m,p);
                 }
             }

             if (o instanceof FSDMsg){
                 FSDMsg m = ctx.get (s);
                 if (m != null) {
                     for (String p: protectFSD)
                         protectField(m,p);
                     for (String p: wipeFSD)
                         wipeField(m,p);
                 }
             }

             if (o instanceof String){
                 String p = ctx.get(s);
                 if (p != null){
                     ctx.put(s, protect (p));
                 }
             }

             if (o instanceof TLVList) {
                 TLVList tlv = ctx.get(s);
                 if (tlv != null) {
                     for (String t: wipeTLV)
                        wipeTag(tlv, t);
                 }
             }
         }
     }

     private void protectField (ISOMsg m, String f) {
         if (m != null) {
             m.set (f, protect (m.getString (f)));
         }
     }

    private void wipeField (ISOMsg m, String f) {
        if (m != null) {
            try {
                Object v = m.getValue(f);
                if (v != null) {
                    if (v instanceof String)
                        m.set(f, ProtectedLogListener.WIPED);
                    else
                        m.set(f, ProtectedLogListener.BINARY_WIPED);
                }
            } catch (ISOException ignored) {
                //ignore, valid routes for some messages in the context may not be valid for others
                //e.g. in transaction switches with protocol conversion
            }
        }
    }

    static void wipeTag(TLVList tlv, String tag) {
        if (tlv == null)
            return;
        try {
            int tagName = Integer.decode(tag);
            if (tlv.hasTag(tagName)) {
                tlv.deleteByTag(tagName);
                tlv.append(tagName, ProtectedLogListener.BINARY_WIPED);
            }
        }
        catch (Throwable ignored) { }
    }

    private void protectField (FSDMsg m, String f) {
         if (f != null) {
             String s = m.get (f);
             if (s != null)
                 m.set (f, ISOUtil.protect (s));
         }
     }

     private void wipeField (FSDMsg m, String f) {
         if (m != null && m.get(f) != null) {
             m.set (f, "*");
         }
     }

     private String protect (String s) {
         return s != null ? ISOUtil.protect (s) : s;
     }

     public void setConfiguration (Configuration cfg) throws ConfigurationException {
         this.protectedEntries = getValues(cfg, "protect-entry");
         this.wipedEntries = getValues(cfg, "wipe-entry");
         this.protectFSD = getValues(cfg, "protect-FSDMsg");
         this.protectISO = getValues(cfg, "protect-ISOMsg");
         this.wipeISO = getValues(cfg, "wipe-ISOMsg");
         this.wipeFSD = getValues(cfg, "wipe-FSDMsg");
         this.wipeTLV = getValues(cfg, "wipe-TLVList");
     }
     private String[] getValues (Configuration cfg, String name) {
         return Arrays.stream(cfg.getAll(name))
             .flatMap(v -> Arrays.stream(v.split(COMMA_SPACE_SEPARATOR)))
             .map(String::trim)
             .filter(v -> !v.isEmpty())
             .toArray(String[]::new);
     }
}
