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

package org.jpos.security;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.jpos.util.Loggeable;

/**
 * Cryptographic Service Message (CSM for short).
 *
 * A message for transporting keys or
 * related information used to control a keying relationship.
 * It is typically the contents of ISOField(123).
 * For more information refer to ANSI X9.17: Financial Institution Key Mangement
 * (Wholesale).
 * </p>
 * @todo add sub-fields support
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */

public class CryptographicServiceMessage implements Loggeable {
    Hashtable fields = new Hashtable();
    Vector orderedTags = new Vector();
    String mcl;

    public static final String MCL_RSI = "RSI";
    public static final String MCL_KSM = "KSM";
    public static final String MCL_RSM = "RSM";
    public static final String MCL_ESM = "ESM";

    public static final String TAG_RCV = "RCV";
    public static final String TAG_ORG = "ORG";
    public static final String TAG_SVR = "SVR";
    public static final String TAG_KD  = "KD" ;
    public static final String TAG_CTP = "CTP";
    public static final String TAG_CTR = "CTR";
    public static final String TAG_ERF = "ERF";

    public static class ParsingException extends Exception {
        public ParsingException() {
            super();
        }
        public ParsingException(String detail) {
            super(detail);
        }
    }

    public CryptographicServiceMessage() {
    }

    /**
     * Creates a CSM and sets its Message Class
     * @param mcl message class name. e.g. MCL_KSM, MCL_RSM...
     */
    public CryptographicServiceMessage(String mcl) {
        setMCL(mcl);
    }

    public void setMCL(String mcl) {
        this.mcl = mcl;
    }

    public String getMCL() {
        return mcl;
    }

    /**
     * adds a field to the CSM
     * @param tag Field Tag
     * @param content Field Content, can't be null, use an empty string ("") instead
     * @throws NullPointerException if tag or content is null
     */
    public void addField(String tag, String content) {
        tag = tag.toUpperCase();
        fields.put(tag, content);
        orderedTags.add(tag);
    }

    /**
     * Returns the field content of a field with the given tag
     * @param tag
     * @return field Field Content, or null if tag not found
     */
    public String getFieldContent(String tag) {
        return (String) fields.get(tag.toUpperCase());
    }


    /**
     * Formats the CSM as a string, suitable for transfer.
     * This is the inverse of parse
     * @return the CSM in string format
     */
    public String toString() {
        String csm="CSM(MCL/" + getMCL() + " ";
        Enumeration tags = orderedTags.elements();
        while (tags.hasMoreElements()) {
            String tag = (String) tags.nextElement();
            csm = csm + tag + "/" + getFieldContent(tag) + " ";
        }

        csm = csm + ")";
        return csm;
    }


    /**
     * dumps CSM basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<csm");
        p.print(" class=\"" + getMCL() + "\"");
        p.println(">");
        Enumeration tags = orderedTags.elements();
        while (tags.hasMoreElements()) {
            String tag = ((String) tags.nextElement());
            p.println(inner + "<field tag=\"" + tag + "\" value=\"" + getFieldContent(tag) + "\"/>");
        }
        p.println(indent + "</csm>");
    }

    /**
     * Parses a csm string
     * @param csmString
     * @return CSM object
     * @throws ParsingException
     */
    public static CryptographicServiceMessage parse(String csmString) throws ParsingException {
        CryptographicServiceMessage csm = new CryptographicServiceMessage();
        StringTokenizer st = new StringTokenizer(csmString, "() \t\n\r\f");
        if (!st.nextToken().equalsIgnoreCase("CSM"))
            throw new ParsingException("Invalid CSM, doesn't start with the \"CSM(\" tag: " + csmString);
        do {
            String field = st.nextToken();
            int separatorIndex = field.indexOf('/');
            if (separatorIndex > 0) {
                String tag = field.substring(0, separatorIndex).toUpperCase();
                String content = "";
                if (separatorIndex < field.length())
                    content = field.substring(separatorIndex + 1);
                if (tag.equalsIgnoreCase("MCL"))
                    csm.setMCL(content);
                else
                    csm.addField(tag, content);
            } else
                throw new ParsingException("Invalid field, doesn't have a tag: " + field);
        } while (st.hasMoreTokens());
        if (csm.getMCL() == null)
            throw new ParsingException("Invalid CSM, doesn't contain an MCL: " + csmString);
        return csm;
    }

}
