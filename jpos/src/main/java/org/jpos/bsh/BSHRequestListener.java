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
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.util.Log;

import java.util.Arrays;
import java.util.HashSet;

/**
 * BSHRequestListener - BeanShell based request listener
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class BSHRequestListener extends Log
    implements ISORequestListener, Configurable
{
    protected static final String MTI_MACRO = "$mti";
    protected HashSet<String> whitelist;
    protected String[] bshSource;
    Configuration cfg;
    public BSHRequestListener () {
        super();
    }
   /**
    * @param cfg
    * <ul>
    *  <li>whitelist - supported message types (example: "1100,1220")
    *  <li>source - BSH script(s) to run (can be more than one)
    * </ul>
    */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        bshSource = cfg.getAll ("source");
        String[] mti = cfg.get ("whitelist", "*").split(",");
        whitelist = new HashSet<>( Arrays.asList(mti) );
    }

    public boolean process (ISOSource source, ISOMsg m) {
        try{
            String mti = m.getMTI ();
            if (!whitelist.contains(mti) && !whitelist.contains("*"))
                mti = "unsupported";

            for (String aBshSource : bshSource) {
                try {
                    Interpreter bsh = new Interpreter();
                    bsh.set("source", source);
                    bsh.set("message", m);
                    bsh.set("log", this);
                    bsh.set("cfg", cfg);

                    int idx = aBshSource.indexOf(MTI_MACRO);
                    String script;

                    if (idx >= 0) {
                        // replace $mti with the actual value in script file name
                        script = aBshSource.substring(0, idx) + mti +
                                aBshSource.substring(idx + MTI_MACRO.length());
                    } else {
                        script = aBshSource;
                    }

                    Object ret= bsh.source(script);

                    // any non-null and non-boolean value is considered "true-ish"
                    // a null return is considered false
                    if (ret != null && (!(ret instanceof Boolean) || (Boolean)ret)) {
                        return true;
                    }

                } catch (Exception e) {
                    warn(e);
                }
            }
        }catch (Exception e){
            warn(e);
            return false;
        }
        //if we reached this far none of the sources handled the request.
        return false;
    }
}

