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

import org.jpos.q2.QFactory;
import org.jpos.q2.QBeanSupport;
import org.jpos.core.ConfigurationException;
import org.jdom.Element;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;
import org.jpos.space.TransientSpace;

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
    LocalSpace sp;
    protected String in, out, unhandled, ready;
    String spaceName;
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
        ready     = e.getChildTextTrim ("ready");
        addListeners ();
        unhandled = e.getChildTextTrim ("unhandled");
    }
    public void startService () {
        sp.addListener (in, this);
        NameRegistrar.register ("mux."+getName (), this);
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

        ISOMsg resp = (ISOMsg) sp.in (key, timeout);

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
        return out + "." +
            m.getMTI().substring(0,2) + 
           (m.hasField(41)?ISOUtil.zeropad((String)m.getValue(41),16) : "")
           + (m.hasField (11) ?
                ISOUtil.zeropad((String) m.getValue(11),6) :
                Long.toString (System.currentTimeMillis()));
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
        if (ready != null) 
            return sp.rdp (ready) != null;
        else
            return true;
    }
}

