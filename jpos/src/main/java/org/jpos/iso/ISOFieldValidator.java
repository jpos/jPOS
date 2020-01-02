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
import org.jpos.core.Configurable;
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
public class ISOFieldValidator implements Configurable, ISOValidator {

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
                throw new ISOVException ( "Error on field " + f.getKey(), f );
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
