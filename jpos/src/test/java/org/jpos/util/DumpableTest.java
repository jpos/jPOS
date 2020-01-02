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

package org.jpos.util;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DumpableTest {

    private Dumpable dumpable;

    @BeforeEach
    public void setUp() throws Exception {
        dumpable = new Dumpable("testingDumpable", "987654321".getBytes());
    }

    @Test
    public void testDumpable() {
        assertThat(dumpable.name, is("testingDumpable"));
        assertThat(dumpable.payload, is("987654321".getBytes()));
    }

    @Test
    public void testDump() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        dumpable.dump(p, ":-o");
        String lineSep     = System.getProperty ("line.separator");
        String expected = ":-o<testingDumpable>" + lineSep + "0000  39 38 37 36 35 34 33 32  31                       987654321" + lineSep + ":-o</testingDumpable>" + lineSep;
        assertThat(baos.toString(), is(expected));
    }
}
