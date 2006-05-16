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
package org.jpos.q2.qbean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceError;
import org.jpos.space.SpaceFactory;
import org.jpos.util.NameRegistrar;

import bsh.EvalError;
import bsh.Interpreter;

public class SpaceLet extends QBeanSupport implements Space {
    Space sp;
    String uri;
    String outScript, outSource;
    String inScript, inSource;
    String rdScript, rdSource;

    public void initService() throws ConfigurationException {
        Element config = getPersist ();
        grabSpace (config.getChild ("space"));
        initSpace (config.getChild ("init"));

        String name = getName();
        if ("spacelet".equals (name))
            name = "default";
        uri = "spacelet:" + name;

        Element e = config.getChild ("out");
        outScript = getScript (e);
        if (e != null)
            outSource = e.getAttributeValue ("source");

        e = config.getChild ("in");
        inScript = getScript (e);
        if (e != null)
            inSource = e.getAttributeValue ("source");

        e = config.getChild ("rd");
        rdScript = getScript (e);
        if (e != null)
            rdSource = e.getAttributeValue ("source");
    }
    public void startService() {
        NameRegistrar.register (uri, this);

        Iterator iter = getPersist().getChildren("run").iterator();
        while (iter.hasNext ()) 
            launch ((Element) iter.next ());
    }
    public void stopService() {
        NameRegistrar.unregister (uri);
    }
    public void out (Object key, Object value) {
        try {
            Interpreter bsh = initInterpreter (key, value);
            synchronized (sp) {
                if (!eval (bsh, outScript, outSource))
                    sp.out (key, value);
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public void out (Object key, Object value, long timeout) {
        try {
            Interpreter bsh = initInterpreter (key, value, timeout);
            synchronized (sp) {
                if (!eval (bsh, outScript, outSource))
                    sp.out (key, value, timeout);
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }

    public Object in  (Object key) {
        try {
            Interpreter bsh = initInterpreter (key);
            synchronized (sp) {
                if (eval (bsh, inScript, inSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.in (key);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public Object rd  (Object key) {
        try {
            Interpreter bsh = initInterpreter (key);
            synchronized (sp) {
                if (eval (bsh, rdScript, rdSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.rd (key);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public Object in  (Object key, long timeout) {
        try {
            Interpreter bsh = initInterpreter (key, timeout);
            synchronized (sp) {
                if (eval (bsh, inScript, inSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.in (key, timeout);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public Object rd  (Object key, long timeout) {
        try {
            Interpreter bsh = initInterpreter (key, timeout);
            synchronized (sp) {
                if (eval (bsh, rdScript, rdSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.rd (key, timeout);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public Object inp (Object key) {
        try {
            Interpreter bsh = initInterpreter (key);
            bsh.set ("probe", true);
            synchronized (sp) {
                if (eval (bsh, inScript, inSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.inp (key);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public Object rdp (Object key) {
        try {
            Interpreter bsh = initInterpreter (key);
            bsh.set ("probe", true);
            synchronized (sp) {
                if (eval (bsh, rdScript, rdSource)) {
                    return bsh.get ("value");
                } else {
                    return sp.rdp (key);
                }
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    private void grabSpace (Element e) {
        sp = SpaceFactory.getSpace (e != null ? e.getText() : "");
    }
    private String getScript (Element e) {
        return (e == null) ? null : e.getText();
    }
    private void launch (Element e) {
        // final Interpreter bsh = initInterpreter ();
        final String script = e.getText();
        final String source = e.getAttributeValue ("source");

        new Thread ("SpaceLet-launch") {
            public void run () {
                try {
                    eval (initInterpreter(), script, source);
                } catch (Throwable t) {
                    getLog().warn (t);
                }
            }
        }.start ();
    }
    private void initSpace (Element e) throws ConfigurationException {
        if (e == null)
            return;

        try {
            eval (
                initInterpreter(), 
                e.getText(), 
                e.getAttributeValue ("source")
            );
        } catch (Throwable t) {
            throw new ConfigurationException (t);
        }
    }
    private Interpreter initInterpreter () throws EvalError {
        Interpreter bsh = new Interpreter ();
        bsh.set ("sp", sp);
        bsh.set ("spacelet", this); 
        bsh.set ("log", getLog());
        return bsh;
    }
    private Interpreter initInterpreter (Object key) throws EvalError {
        Interpreter bsh = initInterpreter ();
        bsh.set ("key", key);
        return bsh;
    }
    private Interpreter initInterpreter (Object key, Object value) 
        throws EvalError
    {
        Interpreter bsh = initInterpreter (key);
        bsh.set ("value", value);
        return bsh;
    }
    private Interpreter initInterpreter 
        (Object key, Object value, long timeout) 
        throws EvalError 
    {
        Interpreter bsh = initInterpreter (key, value);
        bsh.set ("timeout", timeout);
        return bsh;
    }
    private Interpreter initInterpreter (Object key, long timeout) 
        throws EvalError
    {
        Interpreter bsh = initInterpreter (key);
        bsh.set ("timeout", timeout);
        return bsh;
    }
    private boolean eval (Interpreter bsh, String script, String source)
        throws EvalError, FileNotFoundException, IOException
    {
        boolean rc = false;
        if (script != null) {
            Object retValue = bsh.eval (script);
            if (source != null)
                retValue = bsh.source (source);
            if (retValue instanceof Boolean) {
                rc = ((Boolean)retValue).booleanValue ();
            }
        }
        return rc;
    }
}

