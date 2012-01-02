/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jpos.transaction.participant.BSHTransactionParticipant;
import org.junit.Test;

public class SimpleLogSourceTest {

    @Test
    public void testConstructor() throws Throwable {
        SimpleLogSource simpleLogSource = new SimpleLogSource();
        assertNull("simpleLogSource.realm", simpleLogSource.realm);
        assertNull("simpleLogSource.logger", simpleLogSource.logger);
    }

    @Test
    public void testConstructor1() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource simpleLogSource = new SimpleLogSource(logger, "testSimpleLogSourceRealm");
        assertEquals("simpleLogSource.realm", "testSimpleLogSourceRealm", simpleLogSource.realm);
        assertSame("simpleLogSource.logger", logger, simpleLogSource.logger);
    }

    @Test
    public void testError() throws Throwable {
        new BSHTransactionParticipant().error("testSimpleLogSourceDetail", Integer.valueOf(0));
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testErrorThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        logger.addListener(null);
        bSHTransactionParticipant.setLogger(logger, "testSimpleLogSourceRealm");
        try {
            bSHTransactionParticipant.error("testSimpleLogSourceDetail");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testGetLogger() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setLogger(logger, "testSimpleLogSourceRealm");
        Logger result = bSHTransactionParticipant.getLogger();
        assertSame("result", logger, result);
    }

    @Test
    public void testGetRealm() throws Throwable {
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setRealm("testSimpleLogSourceRealm");
        String result = bSHTransactionParticipant.getRealm();
        assertEquals("result", "testSimpleLogSourceRealm", result);
    }

    @Test
    public void testGetRealm1() throws Throwable {
        String result = new BSHTransactionParticipant().getRealm();
        assertNull("result", result);
    }

    @Test
    public void testInfo1() throws Throwable {
        new BSHTransactionParticipant().info("testSimpleLogSourceDetail", "testString");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testInfoThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        logger.addListener(null);
        bSHTransactionParticipant.setLogger(logger, "testSimpleLogSourceRealm");
        try {
            bSHTransactionParticipant.info("testSimpleLogSourceDetail");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testSetLogger() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setLogger(logger, "testSimpleLogSourceRealm");
        assertEquals("(BSHTransactionParticipant) bSHTransactionParticipant.realm", "testSimpleLogSourceRealm",
                ((BSHTransactionParticipant) bSHTransactionParticipant).realm);
        assertSame("(BSHTransactionParticipant) bSHTransactionParticipant.logger", logger,
                ((BSHTransactionParticipant) bSHTransactionParticipant).logger);
    }

    @Test
    public void testSetRealm() throws Throwable {
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        bSHTransactionParticipant.setRealm("testSimpleLogSourceRealm");
        assertEquals("(BSHTransactionParticipant) bSHTransactionParticipant.realm", "testSimpleLogSourceRealm",
                ((BSHTransactionParticipant) bSHTransactionParticipant).realm);
    }

    @Test
    public void testWarning() throws Throwable {
        new BSHTransactionParticipant().warning("testSimpleLogSourceDetail", "");
        assertTrue("Test completed without Exception", true);
    }

    @Test
    public void testWarningThrowsNullPointerException() throws Throwable {
        Logger logger = new Logger();
        SimpleLogSource bSHTransactionParticipant = new BSHTransactionParticipant();
        logger.addListener(null);
        bSHTransactionParticipant.setLogger(logger, "testSimpleLogSourceRealm");
        try {
            bSHTransactionParticipant.warning("testSimpleLogSourceDetail");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }
}
