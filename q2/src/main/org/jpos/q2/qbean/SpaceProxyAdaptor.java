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

import java.util.Set;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jpos.q2.QBeanSupport;

import org.jpos.space.SpaceProxy;

/**
 * Remote Space Proxy Adaptor.
 *
 * @author Alwyn Schoeman
 * @version $Revision$ $Date$
 * @jmx:mbean description="SpaceProxy QBean"
 *                  extends="org.jpos.q2.QBeanSupportMBean"
 */

public class SpaceProxyAdaptor 
    extends QBeanSupport
    implements SpaceProxyAdaptorMBean
{
    private SpaceProxy sp = null;
    private String spaceName = null;

    public SpaceProxyAdaptor () {
        super ();
    }

    protected void startService () throws RemoteException, NamingException {
        if (spaceName == null) 
            sp = new SpaceProxy ();
        else 
            sp = new SpaceProxy (spaceName);
        InitialContext ctx = new InitialContext ();
        ctx.rebind (getName (), sp);
    }

    protected void stopService () {
        sp.shutdown ();
    }

    /**
     * @jmx:managed-attribute description="Space Name"
     */
    public synchronized void setSpaceName (String spaceName) {
        this.spaceName = spaceName;
        setAttr (getAttrs (), "spaceName", spaceName);
        setModified (true);
    }

    /**
     * @jmx:managed-attribute description="Space Name"
     */
    public String getSpaceName () {
        return spaceName;
    }
    
    /**
     * @jmx:managed-attribute description="Space Keys"
     */
    public Set getKeys () {
        return sp.getKeySet ();
    }
}

   
