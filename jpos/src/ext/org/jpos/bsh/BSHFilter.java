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

package org.jpos.bsh;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.util.LogEvent;
import bsh.Interpreter;

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
                bsh.source (source[i]);

                m = (ISOMsg) bsh.get ("message");
            }catch (Exception e) {
                if(e instanceof VetoException) throw (VetoException)e;
                else evt.addMessage (e);
                //throw new VetoException (e);
            }
        }
        return m;
    }
}

