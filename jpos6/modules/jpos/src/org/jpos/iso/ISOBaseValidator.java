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

package org.jpos.iso;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

/**
 * Base Validator class for jPOS composed ISOComponents (ISOMsg).
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOBaseValidator implements ISOValidator, LogSource, ReConfigurable {

    public ISOBaseValidator() {
        super();
    }

    /**
     * Creates the validator.
     * @param breakOnError flag indicating validation abort condition
     */
    public ISOBaseValidator( boolean breakOnError ) {
        this.breakOnError = breakOnError;
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    public boolean breakOnError(){
        return breakOnError;
    }

    public void setBreakOnError( boolean breakOnErr ){
        this.breakOnError = breakOnErr;
    }

    /**
     * Validate field-interdependency.
     * @param m Component to validate
     * @return ISOComponent or ISOVComponent resulting of validation process.
     * @throws ISOException if break-on-error is true and an error succedd.
     */
    public ISOComponent validate( ISOComponent m ) throws ISOException{
        if ( m.getComposite() != m )
            throw new ISOException ( "Can't call validate on non Composite" );
        return m;
    }

    public void setLogger( Logger logger, String realm ){
        this.logger = logger;
        this.realm = realm;
    }

    public Logger getLogger(){
        return logger;
    }

    public String getRealm() {
        return realm;
    }

    protected Logger logger = null;
    protected String realm = null;
    /** Flag used to indicate if validat process break
     * on first error or keep an error set **/
    protected boolean breakOnError = false;
    protected Configuration cfg;
}