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

package org.jpos.transaction.participant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TraceTest {
    Trace trace;
    @Mock
    Context context;
    @Mock
    Configuration configuration;

    @BeforeEach
    public void setUp() throws Exception {
        trace = new Trace();
        given(configuration.get("trace", "org.jpos.transaction.participant.Trace")).willReturn("XXXtestXXX");
        trace.setConfiguration(configuration);
    }

    @Test
    public void testPrepare() {
        int result = trace.prepare(1L, context);
        assertThat(result, is(Trace.PREPARED | Trace.READONLY));
        verify(context).checkPoint("prepare:XXXtestXXX");
    }

    @Test
    public void testCommit() {
        trace.commit(1L, context);
        verify(context).checkPoint("commit:XXXtestXXX");
    }

    @Test
    public void testAbort() {
        trace.abort(1L, context);
        verify(context).checkPoint("abort:XXXtestXXX");
    }

    @Test
    public void testPrepareForAbort() {
        int result = trace.prepareForAbort(1L, context);
        assertThat(result, is(Trace.PREPARED | Trace.READONLY));
        verify(context).checkPoint("prepareForAbort:XXXtestXXX");
    }
}
