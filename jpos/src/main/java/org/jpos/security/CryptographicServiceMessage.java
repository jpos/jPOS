/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.security;

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.*;

/**
 * Cryptographic Service Message (CSM for short).
 *
 * A message for transporting keys or
 * related information used to control a keying relationship.
 * It is typically the contents of ISOField(123).
 * For more information refer to ANSI X9.17: Financial Institution Key Mangement
 * (Wholesale).
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */

@SuppressWarnings("unchecked")
public class CryptographicServiceMessage implements Loggeable {
    Map<String, String> fields = new LinkedHashMap<>();
    String mcl;

    /** Message Class Label for RSI. */
    public static final String MCL_RSI = "RSI";
    /** Message Class Label for KSM (Key Service Message). */
    public static final String MCL_KSM = "KSM";
    /** Message Class Label for RSM. */
    public static final String MCL_RSM = "RSM";
    /** Message Class Label for ESM. */
    public static final String MCL_ESM = "ESM";

    /** Tag name for Receiver. */
    public static final String TAG_RCV = "RCV";
    /** Tag name for Originator. */
    public static final String TAG_ORG = "ORG";
    /** Tag name for Server. */
    public static final String TAG_SVR = "SVR";
    /** Tag name for Key Data. */
    public static final String TAG_KD  = "KD" ;
    /** Tag name for Clear Text PIN. */
    public static final String TAG_CTP = "CTP";
    /** Tag name for Counter. */
    public static final String TAG_CTR = "CTR";
    /** Tag name for Error Flag. */
    public static final String TAG_ERF = "ERF";

    /** Exception thrown when CSM parsing fails. */
    public static class ParsingException extends Exception {

        private static final long serialVersionUID = 6984718759445061L;
        /** Default constructor. */
        public ParsingException() {
            super();
        }
        /**
         * Constructs a ParsingException with the given message.
         * @param detail the error message
         */
        public ParsingException(String detail) {
            super(detail);
        }
    }

    /** Default constructor. */
    public CryptographicServiceMessage() {
    }

    /**
     * Creates a CSM and sets its Message Class
     * @param mcl message class name. e.g. MCL_KSM, MCL_RSM...
     */
    public CryptographicServiceMessage(String mcl) {
        setMCL(mcl);
    }

    /**
     * Sets the message class label.
     * @param mcl the message class label (e.g. MCL_KSM, MCL_RSM)
     */
    public void setMCL(String mcl) {
        this.mcl = mcl;
    }

    /**
     * Returns the message class label.
     * @return the message class label
     */
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
        Objects.requireNonNull(tag, "The tag is required");
        Objects.requireNonNull(content, "The content is required");
        tag = tag.toUpperCase();
        fields.put(tag, content);
    }

    /**
     * Remove field from CSM
     * @param tag Field Tag
     * @throws NullPointerException on null tag
     */
    public void removeField (String tag) {
        Objects.requireNonNull(tag, "The tag is required");
        fields.remove(tag);
    }

    /**
     * Remove fields from CSM
     * @param tags tag list
     * @throws NullPointerException on null tag
     */
    public void removeFields (String... tags) {
        for (String tag : tags) {
            Objects.requireNonNull(tag, "The tag is required");
            fields.remove(tag);
        }
    }

    /**
     * Returns the field content of a field with the given tag.
     * @param tag the field tag (case-insensitive)
     * @return field content, or {@code null} if tag not found
     */
    public String getFieldContent(String tag) {
        return fields.get(tag.toUpperCase());
    }


    /**
     * Formats the CSM as a string, suitable for transfer.
     * This is the inverse of parse
     * @return the CSM in string format
     */
    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder csm = new StringBuilder();
        csm.append("CSM(MCL/");
        csm.append(getMCL());
        csm.append(" ");
        for (String tag : fields.keySet()) {
            csm.append(tag);
            csm.append("/");
            csm.append(getFieldContent(tag));
            csm.append(" ");
        }

        csm.append(")");
        return csm.toString();
    }


    /**
     * dumps CSM basic information
     * @param p a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     * @see org.jpos.util.Loggeable
     */
    @Override
    /** {@inheritDoc} */
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<csm");
        p.print(" class=\"" + getMCL() + "\"");
        p.println(">");
        for (String tag : fields.keySet()) {
            p.println(inner + "<field tag=\"" + tag + "\" value=\"" + getFieldContent(tag) + "\"/>");
        }
        p.println(indent + "</csm>");
    }

    /**
     * Parses a CSM string into a {@link CryptographicServiceMessage} object.
     * @param csmString the CSM string to parse
     * @return the parsed CSM object
     * @throws ParsingException if the string cannot be parsed
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
                else {
                    csm.addField(tag, content);
                }
            } else
                throw new ParsingException("Invalid field, doesn't have a tag: " + field);
        } while (st.hasMoreTokens());
        if (csm.getMCL() == null)
            throw new ParsingException("Invalid CSM, doesn't contain an MCL: " + csmString);
        return csm;
    }

}
