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

/**
 * Base validation-error class. Contains a reference to error details.
 * Error description, Error reject code: optional code used in some
 * financial systems to specifya field reject code. It refer to error.
 * Error Id: A string of " " separated ids. The ids are the fields,
 * subfields, ... ids for the component with error.
 * For example: id="48 0 1" indicates the error was in field 48,
 * subfield 0, subfield 1.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOVError {

    public ISOVError( String Description ) {
        this.description = Description;
    }

    public ISOVError( String Description, String RejectCode ) {
        this.description = Description;
        this.rejectCode = RejectCode;
    }

    public String getRejectCode(){
        return rejectCode;
    }

    public String getId(){
        return id;
    }

    public void setId ( String ID ){
        id = ID;
    }

    public String getDescription() {
        return description;
    }

    /** Used by error parsers to set field tree path **/
    protected String id;
    protected String description = "";
    protected String rejectCode;
    /** default error types **/
    public static final int ERR_INVALID_LENGTH = 1;
    public static final int ERR_INVALID_VALUE = 2;
}