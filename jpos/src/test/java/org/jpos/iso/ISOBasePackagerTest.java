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

package org.jpos.iso;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.jpos.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ISOBasePackagerTest {

    ISOBasePackager iSOBasePackager;
    ISOFieldPackager[] iSOFieldPackagers;
    @Mock
    ISOFieldPackager iSOFieldPackager;

    @BeforeEach
    public void setUp() throws Exception {
        iSOBasePackager = new ISOBasePackager() {
        };
        iSOFieldPackagers = new ISOFieldPackager[] { iSOFieldPackager };
    }

    @Test
    public void testGetDescription() {
        assertThat(iSOBasePackager.getDescription(), is("org.jpos.iso.ISOBasePackagerTest$1"));
    }

    @Test
    public void testGetHeaderLength() {
        iSOBasePackager.setHeaderLength(9876);
        assertThat(iSOBasePackager.getHeaderLength(), is(9876));
    }

    @Test
    public void testGetLogger() {
        Logger logger = mock(Logger.class);
        iSOBasePackager.setLogger(logger, "testRealm");
        assertThat(iSOBasePackager.getLogger(), is(logger));
    }

    @Test
    public void testGetRealm() {
        Logger logger = mock(Logger.class);
        iSOBasePackager.setLogger(logger, "testRealm");
        assertThat(iSOBasePackager.getRealm(), is("testRealm"));
    }

    @Test
    public void testGetMaxValidField() {
        assertThat(iSOBasePackager.getMaxValidField(), is(128));
    }

    @Test
    public void testCreateISOMsg() {
        assertThat(iSOBasePackager.createISOMsg(), is(instanceOf(ISOMsg.class)));
    }

    @Test
    public void testSetFieldPackager() {
        iSOBasePackager.setFieldPackager(iSOFieldPackagers);
        assertThat(iSOBasePackager.getFieldPackager(0), is(iSOFieldPackager));
    }

}
