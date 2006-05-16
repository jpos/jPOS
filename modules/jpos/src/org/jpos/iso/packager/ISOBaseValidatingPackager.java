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

package org.jpos.iso.packager;

import java.util.Hashtable;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOBaseValidator;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFieldValidator;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOValidator;
import org.jpos.iso.validator.ISOVException;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

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
            Hashtable fields = ((ISOMsg)m).getChildren();
            /** Field  validations **/
            for (int i=0; i<fldVld.length; i++) {
                if ( fldVld[i] != null && (c=(ISOComponent) fields.get (new Integer ( ((ISOFieldValidator)fldVld[i]).getFieldId() ))) != null ){
                    try {
                        m.set( fldVld[i].validate( c ) );
                    } catch ( ISOVException e ) {
                        if ( !e.treated() ) {
                            m.set( e.getErrComponent() );
                            e.setTreated( true );
                        }
                        evt.addMessage( "Component Validation Error." );
                        throw e;
                    }
                }
            }
            /** msg validations **/
            try {
                if ( msgVld != null ){
                    for (int i = 0; i < this.msgVld.length; i++) {
                        if ( msgVld[i] != null )
                            m = msgVld[i].validate( m );
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