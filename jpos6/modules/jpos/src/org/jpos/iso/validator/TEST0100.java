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

package org.jpos.iso.validator;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOBaseValidator;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVMsg;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * ONLY TEST PURPOSE.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class TEST0100 extends ISOBaseValidator {

    public TEST0100() {
        super();
    }

    public TEST0100( boolean breakOnError ) {
        super( breakOnError );
    }

    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration( cfg );
        java.util.StringTokenizer st = new java.util.StringTokenizer( cfg.get( "msg-type", ""), " " );
        while ( st.hasMoreTokens() )
            msgTypes.add( st.nextToken() );
    }

    public ISOComponent validate(ISOComponent m) throws org.jpos.iso.ISOException {
        if ( msgTypes.contains ( ((ISOMsg)m).getMTI() ) ){
            LogEvent evt = new LogEvent( this, "validate" );
            try {
                super.validate ( m );
                ISOMsg msg = (ISOMsg)m;
                int[] validFields = { 4,5,7,48 };
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
        } else return m;
    }

    private String makeStrFromArray( int[] validFields ){
        if ( validFields == null ) return null;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < validFields.length; i++){
            result.append( validFields[i] );
            result.append( ", " );
        }
        result.delete( result.length()-2, result.length()-1 );
        return result.toString(  );
    }

    private java.util.HashSet msgTypes=new java.util.HashSet();
}