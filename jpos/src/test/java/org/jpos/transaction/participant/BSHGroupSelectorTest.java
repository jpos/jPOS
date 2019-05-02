/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2019 jPOS Software SRL
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

package org.jpos.transaction.participant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.io.StreamCorruptedException;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.Q2;
import org.jpos.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BSHGroupSelectorTest {
    private static Q2 q2;
    private static TransactionManager tm;

    @BeforeClass
    public static void setUp() {
        q2 = new Q2();
        q2.start();
        if (!q2.ready(10000L)) {
            q2 = null;
            throw new IllegalStateException("Unable to start dummy Q2");
        }
        tm = new TransactionManager();
        tm.setServer(q2);
    }

    @AfterClass
    public static void tearDown() {
        if (q2 != null)
            q2.shutdown(true);
    }


    @Test
    public void testConstructor() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        bSHGroupSelector.setTransactionManager(tm);
        assertNull("bSHGroupSelector.getRealm()", bSHGroupSelector.getRealm());
        assertNull("bSHGroupSelector.getLogger()", bSHGroupSelector.getLogger());
    }

    @Test
    public void testDefaultSelect() throws Throwable {
        String result = new BSHGroupSelector().defaultSelect(100L, new StreamCorruptedException());
        assertEquals("result", "", result);
    }

    @Test
    public void testSelect() throws Throwable {
        String result = new BSHGroupSelector().select(100L, new EOFException());
        assertEquals("result", "", result);
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        bSHGroupSelector.setTransactionManager(tm);
        bSHGroupSelector.setConfiguration(new Element("testBSHGroupSelectorName", "testBSHGroupSelectorUri"));
        assertNull("bSHGroupSelector.prepareForAbortMethod", bSHGroupSelector.prepareForAbortMethod);
        assertNull("bSHGroupSelector.selectMethod", bSHGroupSelector.selectMethod);
        assertNull("bSHGroupSelector.commitMethod", bSHGroupSelector.commitMethod);
        assertNull("bSHGroupSelector.abortMethod", bSHGroupSelector.abortMethod);
        assertNull("bSHGroupSelector.prepareMethod", bSHGroupSelector.prepareMethod);
    }

    @Test
    public void testSetConfigurationThrowsConfigurationException() throws Throwable {
        BSHGroupSelector bSHGroupSelector = new BSHGroupSelector();
        bSHGroupSelector.setTransactionManager(tm);
        try {
            bSHGroupSelector.setConfiguration(null);
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
            assertNull("ex.getNested().getMessage()", ex.getNested().getMessage());
            assertNull("bSHGroupSelector.prepareForAbortMethod", bSHGroupSelector.prepareForAbortMethod);
            assertNull("bSHGroupSelector.selectMethod", bSHGroupSelector.selectMethod);
            assertNull("bSHGroupSelector.commitMethod", bSHGroupSelector.commitMethod);
            assertNull("bSHGroupSelector.abortMethod", bSHGroupSelector.abortMethod);
            assertNull("bSHGroupSelector.prepareMethod", bSHGroupSelector.prepareMethod);
        }
    }
}
