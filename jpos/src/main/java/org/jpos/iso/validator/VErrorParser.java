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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

    protected Logger logger;
    protected String realm;
    private List<ISOVError> errors;

    /**
     * Parse an ISOComponent and get an error vector.
     * @param c Component to parse.
     * @return error vector.
     */
    public List<ISOVError> getVErrors(ISOComponent c) {
        errors = new ArrayList<>();
        getErr(c, errors, "");
        return Collections.unmodifiableList(errors);
    }

    public String parseXMLErrorList(){
        /** @todo !!!!!!!! */
        return "";
    }

    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
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
    @Override
    public void dump(PrintStream p, String indent) {
        /** @todo !!!!!!!!! */
    }

    /**
     * Free errors memory.
     */
    public void resetErrors(){
        errors = null;
    }

    /**
     * Recursive method to get the errors.
     */
    private void getErr(ISOComponent c, List<ISOVError> list, String id) {
        if (c instanceof ISOVField ) {
            Iterator<ISOVError> iter = ((ISOVField) c).errorListIterator();
            while (iter.hasNext()) {
                ISOVError error = iter.next();
                error.setId(id);
                list.add(error);
            }
        }
        else if (c instanceof ISOMsg ) {
            if (c instanceof ISOVMsg ) {
                /** Msg level error **/
                Iterator<ISOVError> iter = ((ISOVMsg) c).errorListIterator();
                while (iter.hasNext()) {
                    ISOVError error = iter.next();
                    error.setId( id );
                    list.add(error);
                }
            }
            /** recursively in childs **/
            Map<Integer, ISOComponent> fields = c.getChildren();
            int max = c.getMaxField();
            for (int i = 0; i <= max ; i++)
                if ((c = fields.get(i)) != null)
                    getErr(c, list, id + Integer.toString(i) + " ");
        }
    }

}
