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
import org.jpos.iso.validator.ISOVException;

/**
 * Validator for ISOField components.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOFieldValidator implements ReConfigurable, ISOValidator {

    public ISOFieldValidator( ) {
        description = "";
    }

    public ISOFieldValidator( String Description ) {
        description = Description;
    }

    public ISOFieldValidator( int maxLen, String Description ) {
        description = Description;
        this.minLen = 0;
        this.maxLen = maxLen;
    }

    public ISOFieldValidator( int minLen, int maxLen, String Description ) {
        description = Description;
        this.minLen = minLen;  this.maxLen = maxLen;
    }

    public ISOFieldValidator( boolean breakOnError, int minLen, int maxLen, String Description ) {
        this( minLen, maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public ISOFieldValidator( boolean breakOnError, int maxLen, String Description ) {
        this( maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public ISOFieldValidator( boolean breakOnError, String Description ) {
        this( Description );
        this.breakOnError = breakOnError;
    }

    /**
     * Create a validator instance specifying breaking if any error
     * during validation process id found.
     * @param breakOnError break condition
     */
    public ISOFieldValidator( boolean breakOnError ) {
        this();
        this.breakOnError = breakOnError;
    }

    /**
     * Default config params are: min-len Minimun length,
     * max-len Max length, break-on-error break condition.
     * @param cfg configuration instance
     * @throws ConfigurationException
     */
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        this.minLen =  cfg.getInt( "min-len", 0 );
        this.maxLen = cfg.getInt( "max-len", 999999 );
        this.breakOnError = cfg.getBoolean( "break-on-error", false );
    }

    public void setMaxLength( int maxLen ){
        this.maxLen = maxLen;
    }

    public void setMinLength( int minLen ){
        this.minLen = minLen;
    }

    public void setBreakOnError( boolean breakOnErr ){
        this.breakOnError = breakOnErr;
    }

    public boolean breakOnError(){
        return breakOnError;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFieldId ( int f ){
        fieldId = f;
    }

    public int getFieldId(){
        return fieldId;
    }

    /**
     * Get the reject code for an error type. At this level is empty.
     * It must be redefined by childs if it is necessary return an
     * error code for specific errors. ISOVError.ERR_INVALID_LENGTH
     * and ISOVErro.ERR_INVALID_VALUE are the defaults.
     * @param ErrType Key for error type.
     * @return the related error code. At this level return null.
     */
    public String getRejCode( int ErrType ){
        /** empty at this level **/
        return null;
    }

    /**
     * Validate a field component. Default for fields only consider
     * field length validations.
     * @param c ISOField component
     * @return an ISOComponent result of validation process. If there area any
     * validation error, then an ISOV component replace original c and it's
     * returned in case of break-on-error condition is false. If break-on-error
     * is false, then an ISOVException containing the ISOV component is raised.
     * @throws ISOException if there are some errors during validation.
     * It contains an ISOV component inside referencing the errors.
     */
    public ISOComponent validate( ISOComponent c ) throws ISOException {
        ISOField f = (ISOField)c;
        Object v = f.getValue();
        int l=0;
        if ( v instanceof byte[] )
            l = ((byte[])v).length;
        else if ( v instanceof String )
            l = ((String)v).length();
        if ( l < minLen || l > maxLen ){
            ISOVError e = new ISOVError(
                    "Invalid Length Error. Length must be in [" + minLen + ", " +
                    maxLen + "]. (Current len: " + l + ") ",
                    getRejCode( ISOVError.ERR_INVALID_LENGTH ) );
            if ( f instanceof ISOVField )
                ((ISOVField)f).addISOVError( e );
            else
                f = new ISOVField( f, e );
            if ( breakOnError )
                throw new ISOVException ( "Error on field " + ((Integer)f.getKey()).intValue(), f );
        }
        return f;
    }

    /** brief field description **/
    protected String description;
    /** field id **/
    protected int fieldId;
    /** field length bounds **/
    protected int minLen = 0, maxLen = 999999;
    /** Flag used to indicate if validat process break on first error or keep an error vector **/
    protected boolean breakOnError = false;
    protected Configuration cfg;
}