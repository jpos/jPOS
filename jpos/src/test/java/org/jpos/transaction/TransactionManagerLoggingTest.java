/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jdom2.Element;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionManagerLoggingTest {
    @Mock
    Q2 q2;
    @Mock
    QFactory factory;

    private TestTransactionManager tm;
    private List<LogEvent> events;

    @BeforeEach
    void setUp() {
        tm = new TestTransactionManager();
        tm.setServer(q2);
        tm.setName("txnmgr-test");
        lenient().when(q2.getFactory()).thenReturn(factory);
        lenient().when(q2.getMeterRegistry()).thenReturn(new SimpleMeterRegistry());

        events = new ArrayList<>();
        Logger logger = new Logger();
        logger.setName("test-txn-logger");
        logger.addListener(ev -> {
            events.add(ev);
            return ev;
        });
        tm.setLogger(logger.getName());
    }

    @Test
    void participantWarningUsesConfiguredAliasTag() throws Exception {
        Element e = new Element("participant");
        e.setAttribute("class", FailingParticipant.class.getName());
        e.setAttribute("realm", "auth-check");
        when(factory.newInstance(FailingParticipant.class.getName())).thenReturn(new FailingParticipant());

        TransactionParticipant participant = tm.createParticipant(e);
        int result = tm.invokePrepare(participant, 7L, new Context());

        assertEquals(TransactionConstants.ABORTED, result);
        LogEvent event = events.stream().filter(ev -> Log.WARN.equals(ev.getTag())).findFirst().orElseThrow();
        assertEquals("auth-check", event.getTags().get("participant"));
    }

    @Test
    void participantWarningFallsBackToSimpleClassName() {
        FailingParticipant participant = new FailingParticipant();
        int result = tm.invokePrepare(participant, 9L, new Context());

        assertEquals(TransactionConstants.ABORTED, result);
        LogEvent event = events.stream().filter(ev -> Log.WARN.equals(ev.getTag())).findFirst().orElseThrow();
        assertEquals(FailingParticipant.class.getSimpleName(), event.getTags().get("participant"));
    }

    static class TestTransactionManager extends TransactionManager {
        int invokePrepare(TransactionParticipant participant, long id, Serializable context) {
            return prepare(participant, id, context);
        }
    }

    public static class FailingParticipant implements TransactionParticipant {
        public void setTransactionManager(TransactionManager tm) {
        }

        @Override
        public int prepare(long id, Serializable context) {
            throw new IllegalStateException("boom");
        }

        @Override
        public void commit(long id, Serializable context) {
        }

        @Override
        public void abort(long id, Serializable context) {
        }
    }
}
