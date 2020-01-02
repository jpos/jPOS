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

package org.jpos.q2.qbean;

import bsh.EvalError;
import bsh.Interpreter;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.Space;
import org.jpos.space.SpaceError;
import org.jpos.space.SpaceFactory;
import org.jpos.util.NameRegistrar;

import java.io.IOException;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class SpaceLet extends QBeanSupport implements Space {
    Space sp;
    String uri;
    String outScript, outSource;
    String pushScript, pushSource;
    String inScript, inSource;
    String rdScript, rdSource;
    String putScript, putSource;

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

        e = config.getChild ("push");
        pushScript = getScript (e);
        if (e != null)
            pushSource = e.getAttributeValue ("source");

        e = config.getChild ("in");
        inScript = getScript (e);
        if (e != null)
            inSource = e.getAttributeValue ("source");

        e = config.getChild ("rd");
        rdScript = getScript (e);
        if (e != null)
            rdSource = e.getAttributeValue ("source");

        e = config.getChild ("put");
        putScript = getScript (e);
        if (e != null)
            putSource = e.getAttributeValue ("source");

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
    public void push (Object key, Object value) {
        try {
            Interpreter bsh = initInterpreter (key, value);
            synchronized (sp) {
                if (!eval (bsh, pushScript, pushSource))
                    sp.out (key, value);
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public void push (Object key, Object value, long timeout) {
        try {
            Interpreter bsh = initInterpreter (key, value, timeout);
            synchronized (sp) {
                if (!eval (bsh, pushScript, pushSource))
                    sp.out (key, value, timeout);
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public void put (Object key, Object value) {
        try {
            Interpreter bsh = initInterpreter (key, value);
            synchronized (sp) {
                if (!eval (bsh, putScript, putSource))
                    sp.put (key, value);
            }
        } catch (Throwable t) {
            throw new SpaceError (t);
        }
    }
    public void put (Object key, Object value, long timeout) {
        try {
            Interpreter bsh = initInterpreter (key, value, timeout);
            synchronized (sp) {
                if (!eval (bsh, putScript, putSource))
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
    public boolean existAny (Object[] keys) {
        return sp.existAny (keys);
    }
    public boolean existAny (Object[] keys, long timeout) {
        return sp.existAny (keys, timeout);
    }
    private void grabSpace (Element e) {
        sp = SpaceFactory.getSpace (e != null ? e.getText() : "");
    }
    private String getScript (Element e) {
        return e == null ? null : e.getText();
    }
    public void nrd (Object key) {
         sp.nrd(key);
    }
    public Object nrd (Object key, long timeout) {
        return sp.nrd(key, timeout);
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
        throws EvalError, IOException
    {
        boolean rc = false;
        if (script != null) {
            Object retValue = bsh.eval (script);
            if (source != null)
                retValue = bsh.source (source);
            if (retValue instanceof Boolean) {
                rc = (Boolean) retValue;
            }
        }
        return rc;
    }
}

