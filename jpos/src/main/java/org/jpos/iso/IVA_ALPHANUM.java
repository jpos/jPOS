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

package org.jpos.iso;

import org.jpos.iso.validator.ISOVException;

/**
 * Validator for ASCII alphanumeric fields.
 *
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class IVA_ALPHANUM extends ISOFieldValidator {

    public IVA_ALPHANUM() {
        super();
    }

    public IVA_ALPHANUM( String Description ) {
        super( Description );
    }

    public IVA_ALPHANUM( int minLen, int maxLen, String Description ) {
        super( minLen, maxLen, Description );
    }

    public IVA_ALPHANUM( int maxLen, String Description ) {
        super( maxLen, Description );
    }

    public IVA_ALPHANUM( boolean breakOnError, String Description ) {
        this( Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUM( boolean breakOnError, int maxLen, String Description ) {
        this( maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUM( boolean breakOnError, int minLen, int maxLen, String Description ) {
        this( minLen, maxLen, Description );
        this.breakOnError = breakOnError;
    }

    /**
     * Validate that component has alphanumeric value.
     *
     * @see ISOUtil#isAlphaNumeric method
     */
    public ISOComponent validate ( ISOComponent f ) throws ISOException {
        ISOField c = (ISOField)f;
        try {
            /** length validation **/
            c = (ISOField)super.validate( c );
            /** alphanum validations **/
            if ( !ISOUtil.isAlphaNumeric( (String)c.getValue() ) ){
                ISOVError e = new ISOVError( "Invalid Value Error. " + c.getValue() + " is not an alphanumeric value. ", getRejCode( ISOVError.ERR_INVALID_VALUE ) );
                if ( c instanceof ISOVField )
                    ((ISOVField)c).addISOVError( e );
                else
                    c = new ISOVField( c, e );
                if ( breakOnError )
                    throw new ISOVException ( "Error on field " + c.getKey(), c );
            }
            return c;
        }
        catch (Exception ex) {
            if ( ex instanceof ISOVException ) throw (ISOVException)ex;
            return c;
        }
    }
}
