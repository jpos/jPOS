/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may
 *    appear in the software itself, if and wherever such third-party
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse
 *    or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
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