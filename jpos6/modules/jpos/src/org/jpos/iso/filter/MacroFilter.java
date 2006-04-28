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

import java.util.Date;
import java.util.TimeZone;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.util.LogEvent;
import org.jpos.util.NameRegistrar;

/**
 * MacroFilter useful to set sequencers, date, unset iso fields, etc.
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class MacroFilter implements ISOFilter, ReConfigurable {
    Sequencer seq;
    Configuration cfg;
    int[] unsetFields  = new int[0];
    int[] validFields  = new int[0];
    public MacroFilter () {
        super();
    }
   /**
    * @param cfg
    * <ul>
    *  <li>sequencer - a sequencer used to store counters
    *  <li>unset - space delimited list of fields to be unset 
    *  <li>valid - space delimited list of valid fields 
    *  <li>comma delimited list of fields to be unset when applying filter
    *  <li>xzy - property named "xyz"
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        try {
            String seqName  = cfg.get ("sequencer", null);
            unsetFields     = ISOUtil.toIntArray (cfg.get ("unset", ""));
            validFields     = ISOUtil.toIntArray (cfg.get ("valid", ""));
            if (seqName != null) {
                seq = (Sequencer) NameRegistrar.get (
                    "sequencer."+cfg.get("sequencer")
                );
            } else if (seq == null) {
                seq = new VolatileSequencer();
            }
        } catch (NameRegistrar.NotFoundException e) {
            throw new ConfigurationException (e);
        }
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
        throws VetoException
    {
        try {
            applyProps (m);
            if (validFields.length > 0) 
                m = (ISOMsg) m.clone (validFields);
            if (unsetFields.length > 0)
                m.unset (unsetFields);
        } catch (ISOException e) {
            evt.addMessage (e);
            throw new VetoException (e);
        }
        return m;
    }
    private void applyProps (ISOMsg m) throws ISOException {
        int maxField = m.getMaxField ();
        for (int i=0; i<=maxField; i++) {
            Object o = null;
            if (m.hasField (i))
                o = m.getValue (i);
            if (o instanceof String) {
                String value = (String) o;
                if (value.length () == 0)
                    continue;
                if (value.equalsIgnoreCase ("$date") )
                    m.set (new ISOField (i, ISODate.getDateTime(new Date())));
                else if ((value.toLowerCase().startsWith ("$date") ) && (value.indexOf("GMT") > 0)) {
                    String zoneID = value.substring(value.indexOf("GMT"));
                    m.set (new ISOField (i, ISODate.getDateTime(new Date(), TimeZone.getTimeZone(zoneID))));
                }                    
                else if (value.charAt (0) == '#')
                    m.set (new ISOField (i,
                      ISOUtil.zeropad (
                        Integer.toString(seq.get (value.substring(1))),6)
                      )
                    );
                else if (value.charAt (0) == '$') {
                    String p = cfg.get (value.substring(1), null);
                    if (p != null)
                        m.set (new ISOField (i, p));
                }
            }
        }
    }
}

