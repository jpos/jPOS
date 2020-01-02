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

package org.jpos.iso.packager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.jpos.iso.ISOException;
import org.junit.jupiter.api.Test;

public class LogPackagerTest {

    static void assertMatch(String message, String regexp, String value){
        Pattern p = Pattern.compile(regexp, Pattern.DOTALL);
        Matcher m = p.matcher(value);
        assert m.matches() :  message;
    }

    @Test
    public void testConstructorThrowsISOException() throws Throwable {
        try {
            new LogPackager();
            fail("Expected ISOException to be thrown");
        } catch (ISOException ex) {
            assertMatch("ex.getMessage()", ".*java.lang.ClassNotFoundException: org.apache.crimson.parser.XMLReaderImpl",
                    ex.getMessage());
            assertNull(ex.getNested(), "ex.getNested()");
        }
    }
}
