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

import org.jpos.iso.validator.ISOVException;

/**
 * Validator for ASCII alphanumeric no-blank filled fields.
 *
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class IVA_ALPHANUMNOBLANK extends IVA_ALPHANUM {

    public IVA_ALPHANUMNOBLANK() {
        super();
    }

    public IVA_ALPHANUMNOBLANK( String Description ) {
        super( Description );
    }

    public IVA_ALPHANUMNOBLANK( int minLen, int maxLen, String Description ) {
        super( minLen, maxLen, Description );
    }

    public IVA_ALPHANUMNOBLANK( int maxLen, String Description ) {
        super( maxLen, Description );
    }

    public IVA_ALPHANUMNOBLANK( boolean breakOnError, String Description ) {
        this( Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUMNOBLANK( boolean breakOnError, int maxLen, String Description ) {
        this( maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUMNOBLANK( boolean breakOnError, int minLen, int maxLen, String Description ) {
        this( minLen, maxLen, Description );
        this.breakOnError = breakOnError;
    }

    /**
     * Validate that component is not blank-filled.
     */
    public ISOComponent validate ( ISOComponent f ) throws ISOException {
        ISOField c = (ISOField)f;
        try {
            /** alphanum validations **/
            c = (ISOField)super.validate( c );
            /** no blank... **/
            if ( ISOUtil.isBlank( (String)c.getValue() ) ){
                ISOVError e = new ISOVError( "Invalid Value Error. It can not be blank-filled. (Current value: " +
                        (String)c.getValue() +") ", getRejCode( ISOVError.ERR_INVALID_VALUE ) );
                if ( c instanceof ISOVField )
                    ((ISOVField)c).addISOVError( e );
                else
                    c = new ISOVField( c, e );
                if ( breakOnError )
                    throw new ISOVException ( "Error on field " + ((Integer)c.getKey()).intValue(), c );
            }
            return c;
        }
        catch (Exception ex) {
            if ( ex instanceof ISOVException ) throw (ISOVException)ex;
            return c;
        }
    }
}
