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

package org.jpos.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.TimerTask;
import java.util.Vector;

import org.jpos.space.TSpace;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class PausedTransactionTest {

    @Test
    public void testCancelExpirationMonitor() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, null);
        pausedTransaction.cancelExpirationMonitor();
        assertNull("pausedTransaction.expirationMonitor",
                junitx.util.PrivateAccessor.getField(pausedTransaction, "expirationMonitor"));
    }

    @Test
    public void testCancelExpirationMonitor1() throws Throwable {
        TimerTask expirationMonitor = new TransactionManager().new PausedMonitor(new Context());
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, expirationMonitor);
        pausedTransaction.cancelExpirationMonitor();
        assertSame("pausedTransaction.expirationMonitor", expirationMonitor,
                junitx.util.PrivateAccessor.getField(pausedTransaction, "expirationMonitor"));
    }

    @Test
    public void testConstructor() throws Throwable {
        List members = new ArrayList();
        Iterator iter = new ArrayList().iterator();
        TransactionManager txnmgr = new TransactionManager();
        TimerTask expirationMonitor = new TSpace();
        PausedTransaction pausedTransaction = new PausedTransaction(txnmgr, 100L, members, iter, true, expirationMonitor);
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertSame("pausedTransaction.expirationMonitor", expirationMonitor,
                junitx.util.PrivateAccessor.getField(pausedTransaction, "expirationMonitor"));
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
        assertSame("pausedTransaction.members()", members, pausedTransaction.members());
        assertSame("pausedTransaction.iterator()", iter, pausedTransaction.iterator());
        assertSame("pausedTransaction.getTransactionManager()", txnmgr, pausedTransaction.getTransactionManager());
    }

    @Test
    public void testDump() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, new TSpace());
        pausedTransaction.dump(new PrintStream(new ByteArrayOutputStream()), "testPausedTransactionIndent");
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testDump1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), false, null);
        pausedTransaction.dump(new PrintStream(new ByteArrayOutputStream()), "testPausedTransactionIndent");
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertFalse("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), false, null);
        try {
            pausedTransaction.dump(null, "testPausedTransactionIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testDumpThrowsNullPointerException1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, null);
        try {
            pausedTransaction.dump(null, "testPausedTransactionIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testForceAbort() throws Throwable {
        byte[] bytes = new byte[2];
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new Stack(), new Scanner(
                new ByteArrayInputStream(bytes)), false, new TSpace());
        pausedTransaction.forceAbort();
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testGetTransactionManager() throws Throwable {
        TransactionManager txnmgr = new TransactionManager();
        PausedTransaction pausedTransaction = new PausedTransaction(txnmgr, 100L, new ArrayList(), new ArrayList().iterator(), true,
                new TSpace());
        TransactionManager result = pausedTransaction.getTransactionManager();
        assertSame("result", txnmgr, result);
    }

    @Test
    public void testId() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 0L, new ArrayList(),
                (new ArrayList()).iterator(), true, new TransactionManager().new PausedMonitor(new Context()));
        long result = pausedTransaction.id();
        assertEquals("result", 0L, result);
    }

    @Test
    public void testId1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, null);
        long result = pausedTransaction.id();
        assertEquals("result", 100L, result);
    }

    @Test
    public void testIsAborting() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), false, new TransactionManager().new PausedMonitor(new Context()));
        boolean result = pausedTransaction.isAborting();
        assertFalse("result", result);
    }

    @Test
    public void testIsAborting1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                (new ArrayList()).iterator(), true, new TransactionManager().new PausedMonitor(new Context()));
        boolean result = pausedTransaction.isAborting();
        assertTrue("result", result);
    }

    @Test
    public void testIterator() throws Throwable {
        Iterator iter = new ArrayList().iterator();
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(), iter, true,
                null);
        Iterator result = pausedTransaction.iterator();
        assertSame("result", iter, result);
    }

    @Test
    public void testMembers() throws Throwable {
        List members = new ArrayList();
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, members,
                new ArrayList().iterator(), true, new TSpace());
        List result = pausedTransaction.members();
        assertSame("result", members, result);
    }

    @Test
    public void testSetResumed() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new Vector(100), new Scanner(
                "testPausedTransactionParam1"), true, new TSpace());
        pausedTransaction.setResumed(true);
        assertTrue("pausedTransaction.isResumed()", pausedTransaction.isResumed());
    }
}
