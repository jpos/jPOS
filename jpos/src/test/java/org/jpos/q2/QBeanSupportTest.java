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

package org.jpos.q2;

import org.jpos.util.Realm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class QBeanSupportTest {
    @Test
    void defaultRealmAppliesComponentTagWhenBeanNameIsKnown() {
        TestQBean bean = new TestQBean();
        bean.setName("mux-alpha");
        assertEquals(Realm.COMM_MUX, bean.getLog().getRealm(), "bean.getLog().getRealm()");
        assertEquals("mux-alpha", bean.getLog().createInfo().getTags().get("component"));
    }

    @Test
    void explicitRealmWinsOverDefaultRealmAndRemovesDefaultComponentTag() {
        TestQBean bean = new TestQBean();
        bean.setName("mux-alpha");
        bean.setRealm("custom/realm");
        assertEquals("custom/realm", bean.getLog().getRealm(), "bean.getLog().getRealm()");
        assertFalse(bean.getLog().createInfo().getTags().containsKey("component"));
    }

    @Test
    void legacyFallbackUsesBeanNameWhenComponentDoesNotOptIn() {
        LegacyQBean bean = new LegacyQBean();
        bean.setName("legacy-bean");
        bean.setLogger("Q2");
        assertEquals("legacy-bean", bean.getLog().getRealm(), "bean.getLog().getRealm()");
    }

    static class TestQBean extends QBeanSupport {
        @Override
        protected String defaultRealm() {
            return Realm.COMM_MUX;
        }
    }

    static class LegacyQBean extends QBeanSupport {
    }
}
