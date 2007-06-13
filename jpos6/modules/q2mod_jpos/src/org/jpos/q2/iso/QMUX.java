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

package org.jpos.q2.iso;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.core.ConfigurationException;
import org.jdom.Element;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;

import org.jpos.iso.MUX;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOException;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @jmx:mbean description="QMUX" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class QMUX 
    extends QBeanSupport
    implements SpaceListener, MUX, QMUXMBean
{
    protected LocalSpace sp;
    protected String in, out, unhandled;
    protected String[] ready;
    protected String spaceName;
    protected int[] key;
    protected String ignorerc;
    List listeners;
    public QMUX () {
        super ();
        listeners = new ArrayList ();
    }
    public void initService () throws ConfigurationException {
        Element e = getPersist ();
        sp        = grabSpace (e.getChild ("space")); 
        in        = e.getChildTextTrim ("in");
        out       = e.getChildTextTrim ("out");
        ignorerc  = e.getChildTextTrim ("ignore-rc");
        key       = toIntArray(e.getChildTextTrim ("key"));
        ready     = toStringArray(e.getChildTextTrim ("ready"));
        addListeners ();
        unhandled = e.getChildTextTrim ("unhandled");
        sp.addListener (in, this);
        NameRegistrar.register ("mux."+getName (), this);
    }
    public void startService () {
        if (getState() == STOPPED) {
            sp.addListener (in, this);
            NameRegistrar.register ("mux."+getName (), this);
        }
    }
    public void stopService () {
        NameRegistrar.unregister ("mux."+getName ());
        sp.removeListener (in, this);
    }

    /**
     * @return MUX with name using NameRegistrar
     * @throws NameRegistrar.NotFoundException
     * @see NameRegistrar
     */
    public static MUX getMUX (String name)
        throws NameRegistrar.NotFoundException 
    {
        return (MUX) NameRegistrar.get ("mux."+name);
    }

    /**
     * @param m message to send
     * @param timeout amount of time in millis to wait for a response
     * @return response or null
     */
    public ISOMsg request (ISOMsg m, long timeout) throws ISOException {
        String key = getKey (m);
        String req = key + ".req";
        sp.out (req, m);
        if (timeout > 0)
            sp.out (out, m, timeout);
        else
            sp.out (out, m);

        ISOMsg resp = null;

        for (;;) {
            resp = (ISOMsg) sp.rd (key, timeout);
            if (shouldIgnore (resp)) 
                continue;
            sp.inp (key);
            break;
        } 
        if (resp == null && sp.inp (req) == null) {
            // possible race condition, retry for a few extra seconds
            resp = (ISOMsg) sp.in (key, 10000);
        }
        return resp;
    }
    public void notify (Object k, Object value) {
        Object obj = sp.inp (k);
        if (obj instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) obj;
            try {
                String key = getKey (m);
                String req = key + ".req";
                if (sp.inp (req) != null) {
                    sp.out (key, m);
                    return;
                }
            } catch (ISOException e) { 
                getLog().warn ("notify", e);
            }
            processUnhandled (m);
        }
    }

    protected String getKey (ISOMsg m) throws ISOException {
        StringBuffer sb = new StringBuffer (out);
        sb.append ('.');
        sb.append (m.getMTI().substring(0,2));
        for (int i=0; i<key.length; i++) {
            int f = key[i];
            String v = m.getString(f);
            if (v != null) {
                if (f == 41) 
                    v = ISOUtil.zeropad (v.trim(), 16); // BIC ANSI to ISO hack
                else if (f == 11) 
                    v = ISOUtil.zeropad (v.trim(), 6); 
                sb.append (v);
            }
        }
        return sb.toString();
    }
    /**
     * @jmx:managed-attribute description="input queue"
     */
    public synchronized void setInQueue (String in) {
        this.in = in;
        getPersist().getChild("in").setText (in);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="input queue"
     */
    public String getInQueue () {
        return in;
    }

    /**
     * @jmx:managed-attribute description="output queue"
     */
    public synchronized void setOutQueue (String out) {
        this.out = out; 
        getPersist().getChild("out").setText (out);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="output queue"
     */
    public String getOutQueue () {
        return out;
    }
    public Space getSpace() {
        return sp;
    }
    /**
     * @jmx:managed-attribute description="unhandled queue"
     */
    public synchronized void setUnhandledQueue (String unhandled) {
        this.unhandled = unhandled;
        getPersist().getChild("unhandled").setText (unhandled);
        setModified (true);
    }
    /**
     * @jmx:managed-attribute description="unhandled queue"
     */
    public String getUnhandledQueue () {
        return unhandled;
    }
    private void addListeners () 
        throws ConfigurationException
    {
        QFactory factory = getFactory ();
        Iterator iter = getPersist().getChildren (
            "request-listener"
        ).iterator();
        while (iter.hasNext()) {
            Element l = (Element) iter.next();
            ISORequestListener listener = (ISORequestListener) 
                factory.newInstance (l.getAttributeValue ("class"));
            factory.setLogger        (listener, l);
            factory.setConfiguration (listener, l);
            addISORequestListener (listener);
        }
    }
    public void addISORequestListener(ISORequestListener l) {
        listeners.add (l);
    }
    public boolean removeISORequestListener(ISORequestListener l) {
    	return listeners.remove(l);
    }
    protected void processUnhandled (ISOMsg m) {
        ISOSource source = m.getSource ();
        if (source != null) {
            Iterator iter = listeners.iterator();
            while (iter.hasNext())
                if (((ISORequestListener)iter.next()).process (source, m))
                    return;
        }
        if (unhandled != null)
            sp.out (unhandled, m, 120000);
    }
    private LocalSpace grabSpace (Element e) 
        throws ConfigurationException
    {
        String uri = e != null ? e.getText() : "";
        Space sp = SpaceFactory.getSpace (uri);
        if (sp instanceof LocalSpace) {
            return (LocalSpace) sp;
        }
        throw new ConfigurationException ("Invalid space " + uri);
    }
    public boolean isConnected() {
        if (ready != null) {
            for (int i=0; i<ready.length; i++)
                if (sp.rdp (ready[i]) != null)
                    return true;
            return false;
        }
        else
            return true;
    }
    private String[] toStringArray (String s) {
        String[] ready = null;
        if (s != null && s.length() > 0) {
            StringTokenizer st = new StringTokenizer (s);
            ready = new String[st.countTokens()];
            for (int i=0; st.hasMoreTokens(); i++)
                ready[i] = st.nextToken();
        }
        return ready;
    }
    private int[] toIntArray (String s) 
        throws ConfigurationException
    {
        if (s == null || s.length() == 0)
            s = "41, 11";
        try {
            int[] k = null;
            StringTokenizer st = new StringTokenizer (s, ", ");
            k = new int[st.countTokens()];
            for (int i=0; st.hasMoreTokens(); i++)
                k[i] = Integer.parseInt(st.nextToken());
            return k;
        } catch (NumberFormatException e) {
            throw new ConfigurationException (e);
        }
    }
    private boolean shouldIgnore (ISOMsg m) {
        if (m != null && ignorerc != null 
            && ignorerc.length() > 0 && m.hasField(39))
        {
            return ignorerc.indexOf(m.getString(39)) >= 0;
        }
        return false;
    }
}

