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

import org.jpos.iso.*;
import org.jpos.iso.validator.ISOVException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.util.Map;

/**
 * Base Packager class envolving validators. It implements
 * ISOValidator interface and define an implementation for validate method.
 * Validation is for composed components.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOBaseValidatingPackager extends ISOBasePackager implements ISOValidator {

    public ISOBaseValidatingPackager() {
        super();
    }

    public ISOComponent validate(ISOComponent m) throws ISOException {
        LogEvent evt = new LogEvent( this, "validate" );
        try {
            ISOComponent c;
            Map fields = m.getChildren();
            /** Field  validations **/
            for (ISOValidator aFldVld : fldVld) {
                if (aFldVld != null && (c = (ISOComponent) fields.get(Integer.valueOf(((ISOFieldValidator) aFldVld).getFieldId()))) != null) {
                    try {
                        m.set(aFldVld.validate(c));
                    } catch (ISOVException e) {
                        if (!e.treated()) {
                            m.set(e.getErrComponent());
                            e.setTreated(true);
                        }
                        evt.addMessage("Component Validation Error.");
                        throw e;
                    }
                }
            }
            /** msg validations **/
            try {
                if ( msgVld != null ){
                    for (ISOBaseValidator aMsgVld : this.msgVld) {
                        if (aMsgVld != null)
                            m = aMsgVld.validate(m);
                    }
                }
            }
            catch (ISOVException ex) {
                evt.addMessage( "Component Validation Error." );
                throw ex;
            }
            return m;
        }
        finally {
            Logger.log( evt );
        }
    }

//    public void setFieldValidator( ISOFieldValidator[] fvlds ){
//        this.fldVld = fvlds;
//    }

    public void setFieldValidator( ISOValidator[] fvlds ){
        this.fldVld = fvlds;
    }


    public void setMsgValidator( ISOBaseValidator[] msgVlds ){
        this.msgVld = msgVlds;
    }

    /** Message level validators **/
    protected ISOBaseValidator[] msgVld;
    /** field validator array. **/
//    protected ISOFieldValidator[] fldVld;
    protected ISOValidator[] fldVld;
}
