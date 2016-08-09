/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class PausedTransactionTest {

    class DummyParticipant implements TransactionParticipant {

        @Override
        public int prepare(long id, Serializable context) {
          return TransactionConstants.PREPARED;
        }

        @Override
        public void commit(long id, Serializable context) {
        }

        @Override
        public void abort(long id, Serializable context) {
        }
    }

    private TimerTask dummyTimerTask = new TimerTask() {
        public void run() { }
    };


    @Test
    public void testConstructor() throws Throwable {
        List members = new ArrayList();
        Iterator iter = new ArrayList().iterator();
        TransactionManager txnmgr = new TransactionManager();
        TimerTask expirationMonitor = dummyTimerTask;
        PausedTransaction pausedTransaction = new PausedTransaction(txnmgr, 100L, members, iter, true, expirationMonitor, null);
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
        assertSame("pausedTransaction.members()", members, pausedTransaction.members());
        assertSame("pausedTransaction.iterator()", iter, pausedTransaction.iterator());
        assertSame("pausedTransaction.getTransactionManager()", txnmgr, pausedTransaction.getTransactionManager());
    }

    @Test
    public void testDump() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), true, dummyTimerTask, null);
        pausedTransaction.dump(new PrintStream(new ByteArrayOutputStream()), "testPausedTransactionIndent");
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testDump1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), false, null, null);
        pausedTransaction.dump(new PrintStream(new ByteArrayOutputStream()), "testPausedTransactionIndent");
        assertEquals("pausedTransaction.id()", 100L, pausedTransaction.id());
        assertFalse("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testDumpThrowsNullPointerException() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), false, null, null);
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
                new ArrayList().iterator(), true, null, null);
        try {
            pausedTransaction.dump(null, "testPausedTransactionIndent");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertNull("ex.getMessage()", ex.getMessage());
        }
    }

    @Test
    public void testForceAbort() throws Throwable {
        List<TransactionParticipant> ltp = new ArrayList();
        ltp.add(new DummyParticipant());
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L
                ,new ArrayList(), ltp.iterator(), false, dummyTimerTask, null);
        pausedTransaction.forceAbort();
        assertTrue("pausedTransaction.isAborting()", pausedTransaction.isAborting());
    }

    @Test
    public void testGetTransactionManager() throws Throwable {
        TransactionManager txnmgr = new TransactionManager();
        PausedTransaction pausedTransaction = new PausedTransaction(txnmgr, 100L, new ArrayList(), new ArrayList().iterator(), true,
                dummyTimerTask, null);
        TransactionManager result = pausedTransaction.getTransactionManager();
        assertSame("result", txnmgr, result);
    }

    @Test
    public void testId() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 0L, new ArrayList(),
                new ArrayList().iterator(), true, new TransactionManager.PausedMonitor(new Context()), null);
        long result = pausedTransaction.id();
        assertEquals("result", 0L, result);
    }

    @Test
    public void testId1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), true, null, null);
        long result = pausedTransaction.id();
        assertEquals("result", 100L, result);
    }

    @Test
    public void testIsAborting() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), false, new TransactionManager.PausedMonitor(new Context()), null);
        boolean result = pausedTransaction.isAborting();
        assertFalse("result", result);
    }

    @Test
    public void testIsAborting1() throws Throwable {
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(),
                new ArrayList().iterator(), true, new TransactionManager.PausedMonitor(new Context()), null);
        boolean result = pausedTransaction.isAborting();
        assertTrue("result", result);
    }

    @Test
    public void testIterator() throws Throwable {
        Iterator iter = new ArrayList().iterator();
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, new ArrayList(), iter, true,
                null, null);
        Iterator result = pausedTransaction.iterator();
        assertSame("result", iter, result);
    }

    @Test
    public void testMembers() throws Throwable {
        List members = new ArrayList();
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L, members,
                new ArrayList().iterator(), true, dummyTimerTask, null);
        List result = pausedTransaction.members();
        assertSame("result", members, result);
    }

    @Test
    public void testSetResumed() throws Throwable {
        List<TransactionParticipant> ltp = new ArrayList();
        ltp.add(new DummyParticipant());
        PausedTransaction pausedTransaction = new PausedTransaction(new TransactionManager(), 100L
                ,new ArrayList(100), ltp.iterator(), true, dummyTimerTask, null);
        pausedTransaction.setResumed(true);
        assertTrue("pausedTransaction.isResumed()", pausedTransaction.isResumed());
    }
}
