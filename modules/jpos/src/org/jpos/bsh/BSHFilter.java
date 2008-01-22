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

package org.jpos.bsh;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.util.LogEvent;

import bsh.Interpreter;
import bsh.TargetError;

/**
 * BSHFilter - BeanShell based filter
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class BSHFilter implements ISOFilter, ReConfigurable {
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

    public ISOMsg filter (ISOChannel channel, ISOMsg m, LogEvent evt) 
        throws VetoException
    {
        String[] source = cfg.getAll ("source");
        for (int i=0; i<source.length; i++) {
            try {
                Interpreter bsh = new Interpreter ();
                bsh.set ("channel", channel);
                bsh.set ("message", m);
                bsh.set ("evt", evt);
                bsh.set ("cfg", cfg);
                Object r = bsh.source (source[i]);
                if (r instanceof ISOMsg)
                    m = (ISOMsg) r;
                else
                    m = (ISOMsg) bsh.get ("message");
            }catch (TargetError e){
               if(e.getTarget() instanceof VetoException)
                   throw (VetoException)e.getTarget();
            }catch (Exception e) {
                if(e instanceof VetoException) throw (VetoException)e;
                else evt.addMessage (e);
                //throw new VetoException (e);
            }
        }
        return m;
    }
}

