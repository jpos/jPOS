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

package org.jpos.bsh;

import bsh.Interpreter;
import bsh.TargetError;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.RawIncomingFilter;
import org.jpos.util.LogEvent;

/**
 * BSHFilter - BeanShell based filter
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class BSHFilter implements RawIncomingFilter, Configurable {
    Configuration cfg;
    public BSHFilter () {
        super();
    }
   /**
    * @param cfg
    * <ul>
    *  <li>source - BSH script(s) to run (can be more than one)
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) throws VetoException {
        return filter (channel, m, null, null, evt);
    }

    public ISOMsg filter (ISOChannel channel, ISOMsg m, byte[] header, byte[] image, LogEvent evt)
        throws VetoException
    {
        String[] source = cfg.getAll ("source");
        for (String aSource : source) {
            try {
                Interpreter bsh = new Interpreter();
                bsh.set("channel", channel);
                bsh.set("message", m);
                if (header != null)
                    bsh.set("header", header);
                if (image != null)
                    bsh.set("image", image);
                bsh.set("evt", evt);
                bsh.set("cfg", cfg);
                Object r = bsh.source(aSource);
                if (r instanceof ISOMsg)
                    m = (ISOMsg) r;
                else
                    m = (ISOMsg) bsh.get("message");
            } catch (TargetError e) {
                if (e.getTarget() instanceof VetoException)
                    throw (VetoException) e.getTarget();
            } catch (Exception e) {
                if (e instanceof VetoException) throw (VetoException) e;
                else evt.addMessage(e);
                //throw new VetoException (e);
            }
        }
        return m;
    }
}

