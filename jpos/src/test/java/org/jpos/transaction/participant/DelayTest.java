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
import static org.jpos.transaction.TransactionConstants.PREPARED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Random;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DelayTest {
    @Mock
    Random random;
    @Mock
    Configuration cfg;
    @Mock
    Serializable context;
    Delay delay;

    @BeforeEach
    public void setUp() throws Exception {
        delay = new Delay();
        delay.random = random;
    }

    @Test
    public void testSetConfiguration() throws ConfigurationException {
        delay.setConfiguration(cfg);
        verify(cfg, atLeastOnce()).getLong("prepare-delay");
        verify(cfg, atLeastOnce()).getLong("commit-delay");
        verify(cfg, atLeastOnce()).getLong("abort-delay");
        verify(cfg, atLeastOnce()).getBoolean("random");
    }

    @Test
    public void testSetConfigurationNoRandomOrSetVars() throws ConfigurationException {
        // given
        given(cfg.getLong("prepare-delay")).willReturn(0L);
        given(cfg.getLong("commit-delay")).willReturn(0L);
        given(cfg.getLong("abort-delay")).willReturn(0L);
        given(cfg.getBoolean("random")).willReturn(false);
        // when
        delay.setConfiguration(cfg);
        // then
        verify(cfg, atLeastOnce()).getLong("prepare-delay");
        verify(cfg, atLeastOnce()).getLong("commit-delay");
        verify(cfg, atLeastOnce()).getLong("abort-delay");
        verify(cfg, atLeastOnce()).getBoolean("random");
    }

    @Test
    public void testPrepare() {
        assertThat(delay.prepare(0L, context), is(PREPARED));
    }

    @Test
    public void testComputeDelay() {
        // This unit test exposes (and verifies fix) of bug found by FindBugs:
        // "Bad attempt to compute absolute value of signed random long"
        // Math.abs can potentially return a negative number
        // see http://stackoverflow.com/questions/2546078/java-random-long-number-in-0-x-n-range
        delay.sleep(5L);
        // should not throw an IllegalArgumentException (timeout value is negative)
        verify(random, atLeastOnce()).nextDouble(); // check we are using the mock Random
    }
}
