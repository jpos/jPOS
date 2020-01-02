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

package org.jpos.iso.validator;

import org.jpos.iso.*;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;

/**
 * Parse ISOComponents and put the errors into a list.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class VErrorParser implements LogSource, Loggeable  {

    /**
     * Parse an ISOComponent and get an error vector.
     * @param c Component to parse.
     * @return error vector.
     */
    public Vector getVErrors( ISOComponent c ) {
        Vector v = new Vector();
        _getErr( c, v, "" );
        _errors = v;
        return _errors;
    }

    public String parseXMLErrorList(){
        /** @todo !!!!!!!! */
        return "";
    }

    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }
    public String getRealm() {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }

    /**
     * Parse error list, and get an dump
     * the xml string representing the list.
     * <pre>
     * Ex:
     * <isomsg>
     *   <field id="2">
     *     <error description="Invalid Len Error" reject-code="101"/>
     *   </field>
     *   <field id="48">
     *     <field id="0">
     *       <field id="1">
     *         <error description="Invalid Value Error" reject-code="102"/>
     *       </field>
     *     </field>
     *   </field>
     *   <error description="Field Expected Error" reject-code="999">
     * </isomsg>
     * </pre>
     * @param p output stream
     * @param indent indent character
     */
    public void dump(PrintStream p, String indent) {
        /** @todo !!!!!!!!! */
    }

    /**
     * Free errors memory.
     */
    public void resetErrors(){
        _errors = null;
    }

    /**
     * Recursive method to get the errors.
     */
    private void _getErr ( ISOComponent c, Vector list, String id ) {
        if ( c instanceof ISOVField ){
            Iterator iter = ((ISOVField)c).errorListIterator();
            while (iter.hasNext()) {
                ISOVError error = (ISOVError)iter.next();
                error.setId( id );
                list.add( error );
            }
        }
        else if ( c instanceof ISOMsg ){
            if ( c instanceof ISOVMsg ){
                /** Msg level error **/
                Iterator iter = ((ISOVMsg)c).errorListIterator();
                while (iter.hasNext()) {
                    ISOVError error = (ISOVError)iter.next();
                    error.setId( id );
                    list.add( error );
                }
            }
            /** recursively in childs **/
            Map fields = c.getChildren();
            int max = c.getMaxField();
            for (int i = 0; i <= max ; i++)
                if ((c=(ISOComponent) fields.get (i)) != null )
                    _getErr( c, list, id +  Integer.toString(i) + " " );
        }
    }

    protected Logger logger = null;
    protected String realm=null;
    private Vector _errors = null;
}
