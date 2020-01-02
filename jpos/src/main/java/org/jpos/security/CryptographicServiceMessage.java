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

package org.jpos.security;

import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Cryptographic Service Message (CSM for short).
 *
 * A message for transporting keys or
 * related information used to control a keying relationship.
 * It is typically the contents of ISOField(123).
 * For more information refer to ANSI X9.17: Financial Institution Key Mangement
 * (Wholesale).
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */

@SuppressWarnings("unchecked")
public class CryptographicServiceMessage implements Loggeable {
    Map<String, String> fields = new HashMap<>();
    List<String> orderedTags = new ArrayList<>();
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

        private static final long serialVersionUID = 6984718759445061L;
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
        Objects.requireNonNull(tag, "The tag is required");
        Objects.requireNonNull(content, "The content is required");
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
        return fields.get(tag.toUpperCase());
    }


    /**
     * Formats the CSM as a string, suitable for transfer.
     * This is the inverse of parse
     * @return the CSM in string format
     */
    @Override
    public String toString() {
        StringBuilder csm = new StringBuilder();
        csm.append("CSM(MCL/");
        csm.append(getMCL());
        csm.append(" ");
        for (String tag : orderedTags) {
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
    public void dump (PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<csm");
        p.print(" class=\"" + getMCL() + "\"");
        p.println(">");
        for (String tag : orderedTags) {
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
