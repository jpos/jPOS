/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

