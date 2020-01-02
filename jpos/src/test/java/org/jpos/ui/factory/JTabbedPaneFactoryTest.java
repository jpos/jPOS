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

package org.jpos.ui.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.event.ChangeEvent;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JTabbedPaneFactoryTest {

    @Test
    public void testConstructor() throws Throwable {
        JTabbedPaneFactory jTabbedPaneFactory = new JTabbedPaneFactory();
        assertEquals(0, jTabbedPaneFactory.actions.size(), "jTabbedPaneFactory.actions.size()");
    }

    @Disabled("test fails because the component is not properly created from the jPOS UI")
    @Test
    public void testStateChanged() throws Throwable {
        JTabbedPaneFactory jTabbedPaneFactory = new JTabbedPaneFactory();
        jTabbedPaneFactory.stateChanged(new ChangeEvent(Integer.valueOf(0)));
        assertEquals(0, jTabbedPaneFactory.actions.size(), "jTabbedPaneFactory.actions.size()");
        assertNull(jTabbedPaneFactory.p, "jTabbedPaneFactory.p");
        assertNull(jTabbedPaneFactory.ui, "jTabbedPaneFactory.ui");
    }
}
