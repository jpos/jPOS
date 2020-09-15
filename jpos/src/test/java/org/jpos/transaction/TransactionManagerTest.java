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

package org.jpos.transaction;

import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.NotActiveException;
import java.io.SerializablePermission;
import java.io.UnsupportedEncodingException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Comment;
import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.SubConfiguration;
import org.jpos.transaction.participant.BSHTransactionParticipant;
import org.jpos.transaction.participant.CheckPoint;
import org.jpos.transaction.participant.Debug;
import org.jpos.transaction.participant.Forward;
import org.jpos.transaction.participant.HasEntry;
import org.jpos.transaction.participant.Join;
import org.jpos.transaction.participant.Trace;
import org.jpos.util.LogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class TransactionManagerTest {
    private TransactionManager transactionManager;
    private List members;

    @BeforeEach
    public void onSetup() {
        transactionManager = new TransactionManager();
        members = new ArrayList();
    }

    @Test
    public void testAbort2() throws Throwable {
        members.add(new Join());
        transactionManager.abort(1, 100L, "", members, false, null, null);
        assertEquals(1, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testAbort3() throws Throwable {
        LogEvent evt = new LogEvent("testTransactionManagerTag", Integer.valueOf(2));
        transactionManager.abort(1, 100L, Boolean.TRUE, members, members.add(new Forward()), evt, null);
        assertEquals(2, evt.getPayLoad().size(), "evt.payLoad.size()");
        assertEquals("          abort: org.jpos.transaction.participant.Forward", evt.getPayLoad().get(1), "evt.payLoad.get(1)");
    }

    @Test
    public void testAbort4() throws Throwable {
        members.add(new Debug());
        LogEvent evt = new LogEvent("testTransactionManagerTag");
        transactionManager.abort(1, 100L, "", members, false, evt, null);
        assertEquals(1, evt.getPayLoad().size(), "evt.payLoad.size()");
        assertEquals("          abort: org.jpos.transaction.participant.Debug", evt.getPayLoad().get(0), "evt.payLoad.get(0)");
    }

    @Test
    public void testAbort5() throws Throwable {
        transactionManager.abort(1, 100L, Long.valueOf(-64L), members, members.add(new Join()), null, null);
        assertEquals(1, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testAbort6() throws Throwable {
        LogEvent evt = new LogEvent();
        transactionManager.abort(1, 100L, new NotActiveException(), members, true, evt, null);
        assertEquals(0, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testAbortThrowsNullPointerException() throws Throwable {
        LogEvent evt = new LogEvent("testTransactionManagerTag");
        try {
            transactionManager.abort(1, 100L, new NotActiveException("testTransactionManagerParam1"), members, members.add(null),
                    evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"Object.getClass()\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, members.size(), "(ArrayList) members.size()");
        }
    }

    @Test
    public void testAbortThrowsNullPointerException2() throws Throwable {
        LogEvent evt = new LogEvent("testTransactionManagerTag", "");
        try {
            transactionManager.abort(1, 100L, new File("testTransactionManagerParam1"), null, true, evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.List.iterator()\" because \"members\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testCheckRetryTask() throws Throwable {
        transactionManager.checkRetryTask();
        transactionManager.checkRetryTask();
        assertNotNull(transactionManager.retryTask, "transactionManager.retryTask");
    }

    @Test
    public void testCheckRetryTask1() throws Throwable {
        transactionManager.checkRetryTask();
        assertNotNull(transactionManager.retryTask, "transactionManager.retryTask");
    }

    @Test
    public void testCheckTailThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.checkTail();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.Space.in(Object)\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.sp, "transactionManager.sp");
            assertEquals(0L, transactionManager.tail, "transactionManager.tail");
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testCommit1() throws Throwable {
        transactionManager.commit(1, 100L, "", members, members.add(new HasEntry()), null, null);
        assertEquals(1, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testCommit2() throws Throwable {
        members.add(new CheckPoint());
        transactionManager.commit(1, 100L, new IOException(), members, false, null, null);
        assertEquals(1, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testCommit3() throws Throwable {
        members.add(new Debug());
        LogEvent evt = new LogEvent();
        transactionManager.commit(1, 100L, Boolean.FALSE, members, false, evt, null);
        assertEquals(1, evt.getPayLoad().size(), "evt.payLoad.size()");
        assertEquals("         commit: org.jpos.transaction.participant.Debug", evt.getPayLoad().get(0), "evt.payLoad.get(0)");
    }

    @Test
    public void testCommit4() throws Throwable {
        LogEvent evt = new LogEvent("testTransactionManagerTag");
        transactionManager.commit(1, 100L, new NotActiveException("testTransactionManagerParam1"), members, true, evt, null);
        assertEquals(0, members.size(), "(ArrayList) members.size()");
    }

    @Test
    public void testCommit5() throws Throwable {
        TransactionParticipant p = new Trace();
        transactionManager.commit(p, 100L, Boolean.FALSE);
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testCommit6() throws Throwable {
        TransactionParticipant p = new CheckPoint();
        transactionManager.commit(p, 100L, "");
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testCommitThrowsNullPointerException() throws Throwable {
        LogEvent evt = new LogEvent();
        try {
            transactionManager.commit(1, 100L, "testString", members, members.add(null), evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"Object.getClass()\" because \"p\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertEquals(1, members.size(), "(ArrayList) members.size()");
        }
    }

    @Test
    public void testCommitThrowsNullPointerException2() throws Throwable {
        LogEvent evt = new LogEvent();
        try {
            transactionManager.commit(1, 100L, new SerializablePermission("testTransactionManagerParam1"), null, true, evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.List.iterator()\" because \"members\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        assertNull(transactionManager.retryTask, "transactionManager.retryTask");
        assertEquals("org.jpos.transaction.TransactionManager", transactionManager
                .getLog().getRealm(), "transactionManager.getLog().getRealm()");
        assertEquals(-1, transactionManager.getState(), "transactionManager.getState()");
        assertTrue(transactionManager.isModified(), "transactionManager.isModified()");
        assertEquals(0L, transactionManager.pauseTimeout, "transactionManager.pauseTimeout");
        assertEquals(5000L, transactionManager.retryInterval, "transactionManager.retryInterval");
    }

    @Test
    public void testGetHead() throws Throwable {
        long result = new TransactionManager().getHead();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testGetKeyThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.getKey(null, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testGetOutstandingTransactions() throws Throwable {
        int result = new TransactionManager().getOutstandingTransactions();
        assertEquals(-1, result, "result");
    }

    @Test
    public void testGetParticipantsThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.getParticipants("testTransactionManagerGroupName");
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Map.get(Object)\" because \"this.groups\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.groups, "transactionManager.groups");
        }
    }

    @Test
    public void testGetTail() throws Throwable {
        long result = new TransactionManager().getTail();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testInitCounterThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.initCounter("testTransactionManagerName", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.Space.rdp(Object)\" because \"this.psp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testInitParticipantsThrowsNullPointerException() throws Throwable {
        Element config = new Element("testTransactionManagerName");
        try {
            transactionManager.initParticipants(config);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Map.put(Object, Object)\" because \"this.groups\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertEquals("testTransactionManagerName", config.getName(), "config.getName()");
        }
    }

    @Test
    public void testInitServiceThrowsConfigurationException() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        transactionManager.setConfiguration(cfg);
        try {
            transactionManager.initService();
            fail("Expected ConfigurationException to be thrown");
        } catch (ConfigurationException ex) {
            assertEquals("queue property not specified", ex.getMessage(), "ex.getMessage()");
            assertNull(ex.getNested(), "ex.getNested()");
            assertNull(transactionManager.queue, "transactionManager.queue");
            assertSame(cfg, transactionManager.getConfiguration(), "transactionManager.getConfiguration()");
            assertEquals(0L, transactionManager.tail, "transactionManager.tail");
            assertTrue(transactionManager.isModified(), "transactionManager.isModified()");
            assertNull(transactionManager.sp, "transactionManager.sp");
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertEquals(0L, transactionManager.head, "transactionManager.head");
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertNull(transactionManager.tailLock, "transactionManager.tailLock");
        }
    }

    @Test
    public void testInitServiceThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.initService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.get(String, String)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.queue, "transactionManager.queue");
            assertNull(transactionManager.getConfiguration(), "transactionManager.getConfiguration()");
            assertEquals(0L, transactionManager.tail, "transactionManager.tail");
            assertTrue(transactionManager.isModified(), "transactionManager.isModified()");
            assertNull(transactionManager.sp, "transactionManager.sp");
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertEquals(0L, transactionManager.head, "transactionManager.head");
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertNull(transactionManager.tailLock, "transactionManager.tailLock");
        }
    }

    @Test
    public void testInitTailLockThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.initTailLock();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.space.Space.put(Object, Object)\" because \"this.sp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.sp, "transactionManager.sp");
        }
    }

    @Test
    public void testNextIdThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.nextId();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot enter synchronized block because \"this.psp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertEquals(0L, transactionManager.head, "transactionManager.head");
        }
    }

    @Test
    public void testPrepare10() throws Throwable {
        HasEntry hasEntry = new HasEntry();
        AbstractList<TransactionParticipant> arrayList = new ArrayList();
        arrayList.add(new Debug());
        int result = transactionManager.prepare(1, 100L, new NotActiveException("testTransactionManagerParam1"), members,
                arrayList.iterator(), members.add(hasEntry), null, null);
        assertEquals(2, members.size(), "(ArrayList) members.size()");
        assertSame(hasEntry, members.get(0), "(ArrayList) members.get(0)");
        assertEquals(0, result, "result");
    }

    @Test
    public void testPrepare2() throws Throwable {
        int result = transactionManager.prepare(new CheckPoint(), 100L, Boolean.FALSE);
        assertEquals(193, result, "result");
    }

    @Test
    public void testPrepare5() throws Throwable {
        int result = transactionManager.prepare(1, 100L, new File("testTransactionManagerParam1"), new ArrayList(), new ArrayList(
                1000).iterator(), true, new LogEvent("testTransactionManagerTag"), null);
        assertEquals(TransactionConstants.ABORTED, result, "result");
    }

    @Test
    public void testPrepare7() throws Throwable {
        transactionManager = new TransactionManager();
        List<TransactionParticipant> members = new ArrayList();
        List<TransactionParticipant> arrayList = new ArrayList();
        BSHTransactionParticipant bSHTransactionParticipant = new BSHTransactionParticipant();
        boolean abort = arrayList.add(bSHTransactionParticipant);
        LogEvent evt = new LogEvent();
        int result = transactionManager.prepare(1, 100L, Boolean.FALSE, members, arrayList.iterator(), abort, evt, null);
        assertEquals(1, members.size(), "(ArrayList) members.size()");
        assertSame(bSHTransactionParticipant, members.get(0), "(ArrayList) members.get(0)");
        assertEquals(1, evt.getPayLoad().size(), "evt.payLoad.size()");
        assertEquals("prepareForAbort: org.jpos.transaction.participant.BSHTransactionParticipant", evt
                .getPayLoad().get(0), "evt.payLoad.get(0)");
        assertEquals(0, result, "result");
    }

    @Test
    public void testPrepare8() throws Throwable {
        AbstractList<TransactionParticipant> arrayList = new ArrayList(1000);
        arrayList.add(new HasEntry());
        LogEvent evt = new LogEvent();
        int result = transactionManager.prepare(1, 100L, Boolean.TRUE, new ArrayList(), arrayList.iterator(), false, evt, null);
        assertEquals(3, evt.getPayLoad().size(), "evt.payLoad.size()");
        assertEquals(TransactionConstants.PREPARED, result, "result");
    }

    @Test
    public void testPrepareForAbort1() throws Throwable {
        int result = transactionManager.prepareForAbort(new Trace(), 100L, new File("testTransactionManagerParam1"));
        assertEquals(64, result, "result");
    }

    @Test
    public void testPrepareForAbort2() throws Throwable {
        int result = transactionManager.prepareForAbort(new Debug(), 100L, Boolean.FALSE);
        assertEquals(129, result, "result");
    }

    @Test
    public void testPrepareThrowsNullPointerException3() throws Throwable {
        List<TransactionParticipant> members = new ArrayList();
        List<TransactionParticipant> arrayList = new ArrayList();
        arrayList.add(new Forward());
        LogEvent evt = new LogEvent();
        try {
            transactionManager.prepare(1, 100L, Boolean.FALSE, members, arrayList.iterator(), false, evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(1, evt.getPayLoad().size(), "evt.payLoad.size()");
            assertEquals("        prepare: org.jpos.transaction.participant.Forward ABORTED", evt.getPayLoad()
                    .get(0), "evt.payLoad.get(0)");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertEquals(0, members.size(), "(ArrayList) members.size()");
        }
    }

    @Test
    public void testPrepareThrowsNullPointerException5() throws Throwable {
        LogEvent evt = new LogEvent("testTransactionManagerTag");
        List<TransactionParticipant> members = new ArrayList();
        List<TransactionParticipant> arrayList = new ArrayList();
        boolean abort = arrayList.add(new Trace());
        try {
            transactionManager.prepare(1, 100L, new NotActiveException(), members, arrayList.iterator(), abort, evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals(1, evt.getPayLoad().size(), "evt.payLoad.size()");
            assertEquals("prepareForAbort: org.jpos.transaction.participant.Trace", evt.getPayLoad().get(0), "evt.payLoad.get(0)");
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertEquals(0, members.size(), "(ArrayList) members.size()");
        }
    }

    @Test
    public void testPrepareThrowsNullPointerException9() throws Throwable {
        LogEvent evt = new LogEvent();
        try {
            transactionManager.prepare(1, 100L, new UnsupportedEncodingException(), members, null, true, evt, null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"java.util.Iterator.hasNext()\" because \"iter\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertNull(transactionManager.groups, "transactionManager.groups");
            assertEquals(0, members.size(), "(ArrayList) members.size()");
        }
    }

    @Test
    public void testPurgeThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.purge(100L, true);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testRecoverThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.recover(1, 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertNull(transactionManager.groups, "transactionManager.groups");
        }
    }

    @Test
    public void testRecoverThrowsNullPointerException1() throws Throwable {
        try {
            transactionManager.recover();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot enter synchronized block because \"this.psp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertEquals(0L, transactionManager.tail, "transactionManager.tail");
            assertNull(transactionManager.groups, "transactionManager.groups");
        }
    }

    @Test
    public void testRetryTaskRun() throws Throwable {
        new TransactionManager().new RetryTask().run();
        assertTrue(true, "Test completed without Exception");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        Configuration cfg = new SimpleConfiguration();
        transactionManager.setConfiguration(cfg);
        assertTrue(transactionManager.debug, "transactionManager.debug");
        assertSame(cfg, transactionManager.getConfiguration(), "transactionManager.getConfiguration()");
        assertEquals(0L, transactionManager.pauseTimeout, "transactionManager.pauseTimeout");
        assertEquals(5000L, transactionManager.retryInterval, "transactionManager.retryInterval");
    }

    @Test
    public void testSetConfigurationThrowsNullPointerException() throws Throwable {
        Configuration cfg = new SubConfiguration();
        try {
            transactionManager.setConfiguration(cfg);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"org.jpos.core.Configuration.getBoolean(String, boolean)\" because \"this.cfg\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertFalse(transactionManager.debug, "transactionManager.debug");
            assertSame(cfg, transactionManager.getConfiguration(), "transactionManager.getConfiguration()");
            assertEquals(0L, transactionManager.pauseTimeout, "transactionManager.pauseTimeout");
            assertEquals(5000L, transactionManager.retryInterval, "transactionManager.retryInterval");
        }
    }

    @Test
    public void testSetStateThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.setState(100L, Integer.valueOf(-1));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testSnapshotThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.snapshot(100L, "testString", Integer.valueOf(-100));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testSnapshotThrowsNullPointerException1() throws Throwable {
        try {
            transactionManager.snapshot(100L, new Comment("testTransactionManagerText"));
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }

    @Test
    public void testStartServiceThrowsNullPointerException1() throws Throwable {
        transactionManager.setName("testTransactionManagerName");
        try {
            transactionManager.startService();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot enter synchronized block because \"this.psp\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
            assertNull(transactionManager.threads, "transactionManager.threads");
            assertNull(transactionManager.getConfiguration(), "transactionManager.getConfiguration()");
            assertEquals(0L, transactionManager.tail, "transactionManager.tail");
            assertNull(transactionManager.groups, "transactionManager.groups");
        }
    }

    @Test
    public void testTailDoneThrowsNullPointerException() throws Throwable {
        try {
            transactionManager.tailDone();
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"str\" is null", ex.getMessage(), "ex.getMessage()");
            }
            assertNull(transactionManager.psp, "transactionManager.psp");
        }
    }
}
