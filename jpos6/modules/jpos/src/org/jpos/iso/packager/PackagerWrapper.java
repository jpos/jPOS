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

package org.jpos.iso.packager;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

/**
 * Wrapper on standard packager
 * @author bharavi gade
 * @version $Revision$ $Date$
 * @see ISOPackager
 */
public abstract class PackagerWrapper 
    implements ISOPackager, LogSource, ReConfigurable
{
    protected Logger logger = null;
    protected String realm = null;
    protected ISOPackager standardPackager = null;
    protected Configuration cfg;

    public PackagerWrapper() {
        super();
    }

    public abstract byte[] pack (ISOComponent c) throws ISOException;

    public abstract int unpack (ISOComponent c, byte[] b) throws ISOException;
    
    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return standardPackager != null ? 
            standardPackager.getFieldDescription (m, fldNumber) : "";
    }
    public void setPackager(ISOPackager packger)
    {
        this.standardPackager=packger;
    }
    public ISOPackager getPackager()
    {
        return standardPackager;
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
        if (standardPackager instanceof LogSource)
            ((LogSource) standardPackager).setLogger (logger, realm);
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    /**
     * requires <code>inner-packager</code> property
     * @param cfg Configuration object
     * @throws ConfigurationException
     */
    public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        String packagerName = cfg.get ("inner-packager");
        try {
            Class p = Class.forName(packagerName);
            setPackager ((ISOPackager) p.newInstance());
            if (standardPackager instanceof Configurable)
                ((Configurable)standardPackager).setConfiguration (cfg);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException ("Invalid inner-packager", e);
        } catch (InstantiationException e) {
            throw new ConfigurationException ("Invalid inner-packager", e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException ("Invalid inner-packager", e);
        }
    }
}

