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
 * Validator for ASCII alphanum no-zero no-blank filled fields.
 *
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class IVA_ALPHANUMNOZERO_NOBLANK extends IVA_ALPHANUMNOZERO {

    public IVA_ALPHANUMNOZERO_NOBLANK() {
        super();
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( String Description ) {
        super( Description );
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( int minLen, int maxLen, String Description ) {
        super( minLen, maxLen, Description );
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( int maxLen, String Description ) {
        super( maxLen, Description );
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( boolean breakOnError, String Description ) {
        this( Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( boolean breakOnError, int maxLen, String Description ) {
        this( maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public IVA_ALPHANUMNOZERO_NOBLANK( boolean breakOnError, int minLen, int maxLen, String Description ) {
        this( minLen, maxLen, Description );
        this.breakOnError = breakOnError;
    }

    /**
     * Validate that the component is not blank-filled.
     */
    public ISOComponent validate ( ISOComponent f ) throws ISOException {
        ISOField c = (ISOField)f;
        try {
            /** no zero... **/
            c = (ISOField)super.validate( c );
            /** no blank **/
            if ( ISOUtil.isBlank( (String)c.getValue() ) ){
                ISOVError e = new ISOVError( "Invalid Value Error. It can not be blank-filled. (Current value: "+
                        c.getValue() +") ", getRejCode( ISOVError.ERR_INVALID_VALUE ) );
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
