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

package org.jpos.util;

import java.text.DecimalFormat;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.nfunk.jep.JEP;


/**
 * JEP utility, using JEP(see http://sourceforge.net/projects/jep/)
 * to deal expression
 * @author <a href="mailto:tzymail@163.com">Zhiyu Tang</a>
 * @version $Revision$ $Date$
 */
public class JepUtil {
    static JepUtil instance = null;
    JEP jep = null;

    static final String BEGIN_TAG = "${";
    static final String END_TAG = "}";
    static final String ANY = "any";

    public JepUtil () {
      jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();
    }
 
   /**
    * Replace macros of the statement.
    * The format of macro is "${fieldid}"
    * @param m - the ISOMsg
    * @param s - the statement
    */
    public String replaceMacro( ISOMsg m, String s )
    throws ISOException
    {
        int beginIdx;
        int tagIdx, endIdx;
        StringBuffer result = new StringBuffer();
        beginIdx = 0;
        tagIdx = s.indexOf( BEGIN_TAG );
        int BEGIN_TAG_LEN = BEGIN_TAG.length();
        int END_TAG_LEN = END_TAG.length();
        while( tagIdx != -1 ){
                endIdx = s.indexOf( END_TAG , tagIdx );
                if( endIdx == -1 )
                  throw new ISOException ( "tags not match" );
                result.append( s.substring(beginIdx , tagIdx));
                String fieldid = s.substring( tagIdx + BEGIN_TAG_LEN , endIdx );
                int field = Integer.valueOf(fieldid).intValue();
                if( !m.hasField( field ) )
                  throw new ISOException ( "tags not match" );
                result.append( m.getValue(field) );
                beginIdx = endIdx + END_TAG_LEN;
                tagIdx = s.indexOf( BEGIN_TAG , beginIdx );
        }
        result.append( s.substring(beginIdx));
        return result.toString();
    }

   /**
    * Return the calculated result
    * @param expression - the jep expression
    */
    public String getResult( String expression )
       throws ISOException
       {
              String valueString = null;
              jep.parseExpression( expression );
              if ( jep.hasError() ) {
                   throw new ISOException( jep.getErrorInfo());
              } else {
                   Object value = jep.getValueAsObject();
                   if (jep.hasError()) {
                       throw new ISOException( jep.getErrorInfo());
                   }
                   if( value  instanceof Double ){
                     DecimalFormat nf = new DecimalFormat();
                     nf.setDecimalSeparatorAlwaysShown( false );
                     nf.setGroupingUsed( false );
                     valueString = nf.format(value);
                   }
                   else if( value  instanceof String )
                        valueString = (String)value;
                }
               return valueString;
    }

   /**
    * Return the judgement
    * @param expression - the jep expression<br>
    * if expression == "any" always return true
    */
    public boolean getResultBoolean( String expression )
        throws ISOException
    {
      if( expression.equals( ANY ) ||
          Integer.valueOf(getResult(expression)).intValue() == 1 )
        return true;
      else
        return false;
    }

   /**
    * Return the calculated result
    * @param m - the ISOMsg
    * @param statement - the expression including macros
    */
    public String getResult( ISOMsg m, String statement )
    throws ISOException
    {
           return getResult( replaceMacro( m, statement ) );
    }

   /**
    * Return the judgement
    * @param m - the ISOMsg
    * @param expression - the jep expression<br>
    * if expression == "any" always return true
    */

    public boolean getResultBoolean( ISOMsg m, String statement )
    throws ISOException
    {
          return getResultBoolean( replaceMacro( m, statement ) );
    }

}

