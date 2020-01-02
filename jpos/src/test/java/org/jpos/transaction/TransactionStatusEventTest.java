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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.jpos.transaction.TransactionStatusEvent.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransactionStatusEventTest {
    TransactionStatusEvent transactionStatusEvent;

    @BeforeEach
    public void setUp() throws Exception {
        int session = 1;
        State state = State.ABORTING;
        long id = 2L;
        String info = "inforString";
        Serializable context = "someContextObject";
        transactionStatusEvent = new TransactionStatusEvent(session, state, id, info, context);
    }

    @Test
    public void testToString() {
        String expected = "01 00000002 Aborting inforString";
        assertThat(transactionStatusEvent.toString(), is(expected));
    }

    @Test
    public void testGetSession() {
        assertThat(transactionStatusEvent.getSession(), is(1));
    }

    @Test
    public void testGetId() {
        assertThat(transactionStatusEvent.getId(), is(2L));
    }

    @Test
    public void testGetInfo() {
        assertThat(transactionStatusEvent.getInfo(), is("inforString"));
    }

    @Test
    public void testGetState() {
        assertThat(transactionStatusEvent.getState(), is(State.ABORTING));
    }

    @Test
    public void testGetStateAsString() {
        assertThat(transactionStatusEvent.getStateAsString(), is("Aborting"));
    }

    @Test
    public void testGetContext() {
        assertThat(transactionStatusEvent.getContext(), is((Serializable) "someContextObject"));
    }

    @Test
    public void testStateAsString() {
        List<String> expected = Arrays.asList("Ready", "Preparing", "Preparing for abort", "Commiting", "Aborting",
          "Done", "Paused");
        State[] values = State.values();
        for (State state : values) {
            assertThat(expected, containsInAnyOrder(state.stateAsString));
        }
    }

    @Test
    public void testStateIntValues() {
        assertThat(State.READY.intValue(), is(0));
        assertThat(State.PREPARING.intValue(), is(1));
        assertThat(State.PREPARING_FOR_ABORT.intValue(), is(2));
        assertThat(State.COMMITING.intValue(), is(3));
        assertThat(State.ABORTING.intValue(), is(4));
        assertThat(State.DONE.intValue(), is(5));
        assertThat(State.PAUSED.intValue(), is(6));
    }
}
