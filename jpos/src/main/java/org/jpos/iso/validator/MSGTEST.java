/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.iso.validator;

import org.jpos.iso.*;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * Tester to validate msg.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class MSGTEST extends ISOBaseValidator {

    public MSGTEST() {
        super();
    }

    public MSGTEST( boolean breakOnError ) {
        super(breakOnError);
    }

    private String makeStrFromArray( int[] validFields ){
        if ( validFields == null ) return null;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < validFields.length; i++){
            result.append( validFields[i] );
            result.append( ", " );
        }
        result.delete( result.length()-2, result.length()-1 );
        return result.toString(  );
    }

    public ISOComponent validate(ISOComponent m) throws org.jpos.iso.ISOException {
        LogEvent evt = new LogEvent( this, "validate" );
        try {
            super.validate ( m );
            ISOMsg msg = (ISOMsg)m;
            int[] validFields = { 3,7,11 };
            if ( !msg.hasFields( validFields ) ){
                ISOVError e = new ISOVError( "Fields " + makeStrFromArray( validFields ) + " must appear in msg.", "001" );
                if ( msg instanceof ISOVMsg )
                    ((ISOVMsg)msg).addISOVError( e );
                else
                    msg = new ISOVMsg( msg, e );
                if ( breakOnError )
                    throw new ISOVException ( "Error on msg. " , msg );
            }
            return msg;
        } catch ( ISOVException ex ) {
            throw ex;
        } finally {
            Logger.log( evt );
        }
    }
}