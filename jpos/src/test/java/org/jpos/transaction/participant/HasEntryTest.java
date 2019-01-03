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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HasEntryTest {
    HasEntry hasEntry;
    @Mock
    Context context;
    @Mock
    Configuration configuration;

    @Before
    public void setUp() throws Exception {
        hasEntry = new HasEntry();
        given(configuration.get("name")).willReturn("XYZtest");
        hasEntry.setConfiguration(configuration);
    }

    @Test
    public void testPrepare() {
        assertThat(hasEntry.prepare(1L, context), is(HasEntry.PREPARED | HasEntry.NO_JOIN | HasEntry.READONLY));
    }

    @Test
    public void testSelect() {
        // given
        given(context.get("XYZtest")).willReturn("someStuffs");
        given(configuration.get("yes", "UNKNOWN")).willReturn("someStuffs");
        // when
        String result = hasEntry.select(2L, context);
        // then
        assertThat(result, is("someStuffs"));
    }

    @Test
    public void testSelectNo() {
        // given
        given(context.get("XYZtest")).willReturn(null);
        given(configuration.get("no", "UNKNOWN")).willReturn("differentStuffs");
        // when
        String result = hasEntry.select(3L, context);
        // then
        assertThat(result, is("differentStuffs"));
    }
}
