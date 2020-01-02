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

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Wrapper class resulting from process of validating an ISOField
 * instance. Contains details of the original field and validation-error
 * details too. Normally in validation process when an error is detected
 * by validator in some field, then the field is replaced by an instance
 * of this class, containning error details.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ISOVField extends ISOField implements ISOVErrorList {

    private static final long serialVersionUID = -2503711799295775875L;

    /**
     * Creates the vfield.
     * @param Source original field instance.
     */
    public ISOVField( ISOField Source ) {
        super();
        this.fieldNumber = Source.fieldNumber;
        this.value = Source.value;
    }

    public ISOVField( ISOField Source, ISOVError FirstError ) {
        this( Source );
        this.errors.addLast( FirstError );
    }

    public boolean addISOVError(ISOVError Error) {
        return errors.add( Error );
    }

    public ListIterator errorListIterator() {
        return errors.listIterator();
    }

    /** list of errors **/
    protected LinkedList errors = new LinkedList(  );
}
