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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.jpos.util.Logger;
import org.junit.jupiter.api.Test;

public class PostPackagerTest {

    @Test
    public void testConstructor() throws Throwable {
        PostPackager postPackager = new PostPackager();
        assertNotNull(postPackager.p127, "postPackager.p127");
        assertNull(postPackager.getLogger(), "postPackager.getLogger()");
        assertEquals(129, postPackager.fld.length, "postPackager.fld.length");
        assertNull(postPackager.getRealm(), "postPackager.getRealm()");
    }

    @Test
    public void testPostPrivatePackagerConstructor() throws Throwable {
        PostPackager.PostPrivatePackager postPrivatePackager = new PostPackager.PostPrivatePackager();
        assertNull(postPrivatePackager.getLogger(), "postPrivatePackager.getLogger()");
        assertEquals(26, postPrivatePackager.fld127.length, "postPrivatePackager.fld127.length");
        assertNull(postPrivatePackager.getRealm(), "postPrivatePackager.getRealm()");
    }

    @Test
    public void testSetLogger() throws Throwable {
        PostPackager postPackager = new PostPackager();
        Logger logger = Logger.getLogger("testPostPackagerName");
        postPackager.setLogger(logger, "testPostPackagerRealm");
        assertNotNull(postPackager.p127, "postPackager.p127");
        assertSame(logger, postPackager.getLogger(), "postPackager.getLogger()");
        assertEquals("testPostPackagerRealm", postPackager.getRealm(), "postPackager.getRealm()");
    }
}
