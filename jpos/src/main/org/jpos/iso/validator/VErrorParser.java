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

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOVError;
import org.jpos.iso.ISOVField;
import org.jpos.iso.ISOVMsg;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;

/**
 * Parse ISOComponents and put the errors into a list.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
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
            Hashtable fields = ((ISOMsg)c).getChildren();
            int max = ((ISOMsg)c).getMaxField();
            for (int i = 0; i <= max ; i++)
                if ((c=(ISOComponent) fields.get (new Integer (i))) != null )
                    _getErr( c, list, id +  String.valueOf( ( new Integer (i)).intValue() ) + " " );
        }
    }

    protected Logger logger = null;
    protected String realm=null;
    private Vector _errors = null;
}