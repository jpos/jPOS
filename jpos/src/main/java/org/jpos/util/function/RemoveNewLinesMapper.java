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

package org.jpos.util.function;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.nio.ByteBuffer;

/**
 * Mapper for removing newlines from the output of writing LogEvents to a stream.
 *
 * Configuration options allow you to optionally combine multiple spaces into a single space and
 * opting not to add a newline at the end of the data.
 *
 * Example: <br>
 *     <pre>
 *         <output-mapper class="org.jpos.util.function.RemoveNewLinesMapper">
 *             <properties name="combine-spaces" value="true"/>
 *             <properties name="newline-at-end" value="false/>
 *         </output-mapper>
 *     </pre><br>
 *
 * <b>NB. Do not set combine-spaces to true if you have data where spaces are significant.</b>
 *
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public class RemoveNewLinesMapper implements ByteArrayMapper, Configurable {
    boolean combineSpaces = false;
    boolean newLineAtEnd = true;
    final byte SPACE = ' ';
    byte[] NEWLINE_SEPARATORS = System.lineSeparator().getBytes();

    @Override
    public byte[] apply(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        boolean prevSpace = false;
        for (byte aByte : bytes) {
            if (aByte == SPACE) {
                if (combineSpaces) {
                    prevSpace = true;
                } else {
                    buffer.put(aByte);
                }
            } else {
                if (combineSpaces && prevSpace) {
                    buffer.put(SPACE);
                    prevSpace = false;
                }
                if (!isNewLine(aByte)) {
                    buffer.put(aByte);
                }
            }
        }
        // Solve the mystery of the missing trailing spaces.
        if (combineSpaces && prevSpace) {
            buffer.put(SPACE);
        }
        if (newLineAtEnd) {
            buffer.put(NEWLINE_SEPARATORS);
        }
        buffer.flip();
        byte[] result = new byte[buffer.limit()];
        buffer.get(result);
        return result;
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        combineSpaces = cfg.getBoolean("combine-spaces", false);
        newLineAtEnd = cfg.getBoolean("newline-at-end", true);
    }

    private boolean isNewLine(byte aByte) {
        boolean isNewLine = false;
        for (byte b : NEWLINE_SEPARATORS) {
            if (aByte == b) {
                isNewLine = true;
                break;
            }
        }
        return isNewLine;
    }
}
