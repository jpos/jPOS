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

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.validator.ISOVException;

/**
 * Validator for ASCII numeric fields. By default radix is 10.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class IVA_NUM extends ISOFieldValidator {

    public IVA_NUM(  ) {
        super(  );
    }

    /**
     * radix = 10.
     * @param Description Brief description.
     */
    public IVA_NUM( String Description ) {
        super( Description );
    }

    public IVA_NUM( String Description, int radix ) {
        super( Description );
        this.radix = radix;
    }

    public IVA_NUM( int maxLen, String Description ) {
        super( maxLen, Description );
    }

    public IVA_NUM( int maxLen, String Description, int radix ) {
        super( maxLen, Description );
        this.radix = radix;
    }

    /**
     * Create the validator. Radix is 10.
     * @param minLen min length.
     * @param maxLen max length
     * @param Description Validator description
     */
    public IVA_NUM( int minLen, int maxLen, String Description ) {
        super( minLen, maxLen, Description );
    }

    /**
     * Create the validator
     * @param minLen min length.
     * @param maxLen max length
     * @param Description Validator description
     * @param radix numeric radix for numeric validation
     */
    public IVA_NUM( int minLen, int maxLen, String Description, int radix ) {
        super( minLen, maxLen, Description );
        this.radix = radix;
    }

    public IVA_NUM( boolean breakOnError, String Description ) {
        this( Description );
        this.breakOnError = breakOnError;
    }

    public IVA_NUM( boolean breakOnError, String Description, int radix ) {
        this( Description, radix );
        this.breakOnError = breakOnError;
    }

    public IVA_NUM( boolean breakOnError, int maxLen, String Description ) {
        this( maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public IVA_NUM( boolean breakOnError, int maxLen, String Description, int radix ) {
        this( maxLen, Description, radix );
        this.breakOnError = breakOnError;
    }

    public IVA_NUM( boolean breakOnError, int minLen, int maxLen, String Description ) {
        this( minLen, maxLen, Description );
        this.breakOnError = breakOnError;
    }

    public IVA_NUM( boolean breakOnError, int minLen, int maxLen, String Description, int radix ) {
        this( minLen, maxLen, Description, radix );
        this.breakOnError = breakOnError;
    }

    /**
     * Configure the validator. @see ISOFieldValidator class.
     * Take config param "radix" wich specify the numeric radix.
     * @param cfg configuration instance
     * @throws ConfigurationException
     */
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration( cfg );
        this.radix = cfg.getInt( "radix", 10 );
    }

    /**
     * Validate numeric condition. @see ISOFieldValidator class.
     * @param f ISOField to validate
     * @return see validate method in ISOFieldValidator class.
     * @throws ISOException if any validation error.
     */
    public ISOComponent validate( ISOComponent f ) throws ISOException {
        ISOField c = (ISOField)f;
        c = (ISOField)super.validate( c );
        if ( !ISOUtil.isNumeric( (String)c.getValue(), this.radix ) ){
            ISOVError e = new ISOVError(
                    "Invalid Value Error. " + c.getValue() +
                    " is not a numeric value in radix " +
                    this.radix, getRejCode( ISOVError.ERR_INVALID_VALUE ) );
            if ( c instanceof ISOVField )
                ((ISOVField)c).addISOVError( e );
            else
                c = new ISOVField( c, e );
            if ( breakOnError )
                throw new ISOVException ( "Error on field " + c.getKey(), c );
        }
        return c;
    }

    /** by default is decimal **/
    protected int radix = 10;
}
