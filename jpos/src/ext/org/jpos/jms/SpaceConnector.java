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

package org.jpos.jms;

import org.jpos.iso.ISOUtil;
import org.jpos.space.TransientSpace;
import org.jpos.space.LocalSpace;
import org.jpos.space.SpaceListener;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.Q2ConfigurationException;
import javax.jms.*;

/**
 * JMS Space Connector for taking JMS objects off the space, unpacking the contents, returning
 * the result to the space again.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @since 1.4.6
 * @jmx:mbean description="JMS SpaceConnector MBean"
 *  extends="org.jpos.q2.QBeanSupportMBean"
 */
public class SpaceConnector extends QBeanSupport implements SpaceListener,SpaceConnectorMBean {
    private LocalSpace space;
    private String fromKey = null;
    private String toKey = null;
    private String spaceName = null;
    
    private int byteBufSize = 200;

    public SpaceConnector () {
        super ();
    }

    protected void startService () throws Exception {
        if ((spaceName == null) || (toKey == null) || (fromKey == null))
            throw new Q2ConfigurationException ("Space parameters need to be configured");
        if (spaceName.equals(""))
            space = TransientSpace.getSpace ();
        else
            space = TransientSpace.getSpace (spaceName);
        listen2Space ();
    }

    protected void stopService () {
        deaf2Space ();
    }

    private void listen2Space () {
        space.addListener (fromKey, this);
    }

    private void deaf2Space () {
        space.removeListener (fromKey, this);
    }

    public void notify (Object key, Object value) {
        Object o = space.inp (key);
        if (o != null)
            if (o instanceof ObjectMessage) {
                log.info ("Received ObjectMessage");
                ObjectMessage obj = (ObjectMessage) o;
                try {
                    space.out (toKey, obj.getObject ());
                } catch (JMSException e) {
                    log.error ("Could not extract object, dropping", e);
                }
            } else if (o instanceof BytesMessage) {
                log.info ("Received BytesMessage");
                BytesMessage bm = (BytesMessage) o;
                byte[] totalBuf = new byte[0];
                byte[] tempBuf = new byte[byteBufSize];
                try {
                    bm.reset ();
                    log.info ("BM reset");
                    int numRead = bm.readBytes (tempBuf, byteBufSize);
                    log.info ("Read "+numRead+" bytes");
                    while (numRead != -1) {
                        if (numRead < byteBufSize)
                            ISOUtil.trim (tempBuf, numRead);
                        totalBuf = ISOUtil.concat (totalBuf,0,totalBuf.length,tempBuf,0,numRead);
                        log.info ("Total buf now="+ISOUtil.hexString (totalBuf));
                        numRead = bm.readBytes (tempBuf, byteBufSize);
                        log.info ("Read "+numRead+" bytes");
                    }
                } catch (JMSException e) {
                    log.error ("Could not extract byte array, dropping", e);
                }
                log.info ("TotalByteBuf="+ISOUtil.hexString (totalBuf));
                space.out (toKey, totalBuf);
            }
    }

    /**
     * @jmx:managed-attribute description="Source key"
     */
    public synchronized void setFromKey (String fromKey) {
        if (getState() == STARTED)
            deaf2Space ();
        this.fromKey = fromKey;
        setAttr (getAttrs(), "fromKey", fromKey);
        setModified (true);
        if (getState() == STARTED)
            listen2Space ();
    }

    /**
     * @jmx:managed-attribute description="Source key"
     */
    public String getFromKey () {
        return fromKey;
    }

    /**
     * @jmx:managed-attribute description="Destination key"
     */
    public synchronized void setToKey (String toKey) {
        this.toKey = toKey;
        setAttr (getAttrs(), "toKey", toKey);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Destination key"
     */
    public String getToKey () {
        return toKey;
    }

    /**
     * @jmx:managed-attribute description="Space"
     */
    public synchronized void setSpace (String spaceName) {
        this.spaceName = spaceName;
        setAttr (getAttrs(), "space", spaceName);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space"
     */
    public String getSpace () {
        return spaceName;
    }
}

