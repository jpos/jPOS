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

package org.jpos.iso.packager;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.validator.ISOVException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.util.Map;

/**
 * Tester validating packager for subfields in field 48.
 *
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class CTCSubFieldPackager extends ISOBaseValidatingPackager {

    public CTCSubFieldPackager() {
        super();
    }

    public byte[] pack ( ISOComponent c ) throws ISOException {
        try     {
            Map tab = c.getChildren();
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < fld.length; i++ ) {
                ISOField f = (ISOField) tab.get (i);
                if ( f != null ) {
                    sb.append ( new String( fld[i].pack( f ) ) );
                }
            }
            return sb.toString().getBytes();
        }
        catch ( Exception ex ) {
            throw new ISOException ( this.getRealm() + ": " +  ex.getMessage(), ex );
        }
    }

    public int unpack ( ISOComponent m, byte[] b ) throws ISOException {
        LogEvent evt = new LogEvent ( this, "unpack" );
        int consumed = 0;
        for ( int i=0; consumed < b.length ; i++ ) {
            ISOComponent c = fld[i].createComponent( i );
            consumed += fld[i].unpack ( c, b, consumed );
            if ( logger != null )       {
                evt.addMessage ("<unpack fld=\"" + i
                                +"\" packager=\""
                                +fld[i].getClass().getName()+ "\">");
                evt.addMessage ("  <value>"
                                +c.getValue().toString()
                                + "</value>");
                evt.addMessage ("</unpack>");
            }
            m.set(c);
        }
        Logger.log (evt);
        return consumed;
    }

    /**
     * Always return false.
     * <br><br>
     **/
    protected boolean emitBitMap() {
        return false;
    }

    public ISOComponent validate( ISOComponent c ) throws org.jpos.iso.ISOException {
        LogEvent evt = new LogEvent( this, "validate" );
        try {
            Map tab = c.getChildren();
            for ( int i = 0; i < fldVld.length; i++ ) {
                ISOField f = (ISOField) tab.get (i);
                if ( f != null )
                    c.set( fldVld[i].validate( f ) );
            }
            return c;
        } catch ( ISOVException ex ) {
            if ( !ex.treated() ) {
                c.set( ex.getErrComponent() );
                ex.setTreated( true );
            }
            evt.addMessage( ex );
            throw ex;
        } finally {
            Logger.log( evt );
        }
    }
}
