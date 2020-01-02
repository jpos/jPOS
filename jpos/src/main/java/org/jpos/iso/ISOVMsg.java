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
 * Wrapper class resulting from process of validating an ISOMsg
 * instance. Contains details of the original msg and validation-error
 * details too. Normally in validation process when an error is detected
 * by validator in msg, then the msg is replaced by an instance
 * of this class, containning error details.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ISOVMsg extends ISOMsg implements ISOVErrorList {

    private static final long serialVersionUID = 443461124206801037L;

    /**
     * Copy properties from parent.
     * @param Source original instance.
     */
    private void copyFromParent( ISOMsg Source ){
        this.packager = Source.packager;
        this.fields = Source.fields;
        this.dirty = Source.dirty;
        this.maxFieldDirty = Source.maxFieldDirty;
        this.header = Source.header;
        this.fieldNumber = Source.fieldNumber;
        this.maxField = Source.maxField;
        this.direction = Source.direction;
    }

    /**
     * Create a message from original instance adding error data.
     * @param Source Original msg instance.
     */
    public ISOVMsg( ISOMsg Source ) {
        /** @todo Try best strategy */
        copyFromParent( Source );
    }

    public ISOVMsg( ISOMsg Source, ISOVError FirstError ) {
        /** @todo Try best strategy */
        copyFromParent( Source );
        addISOVError( FirstError );
    }

    /**
     * Add an error component to the list of errors.
     * @param Error Error instance to add.
     * @return True if the list of errors change after operation.
     */
    public boolean addISOVError(ISOVError Error) {
        return errors.add( Error );
    }

    /**
     * Get an error iterator instance.
     * @return iterator.
     */
    public ListIterator errorListIterator() {
        return errors.listIterator();
    }

    /** list of errors **/
    protected LinkedList errors = new LinkedList(  );
}
