/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2022 jPOS Software SRL
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

package org.jpos.core;

import org.jpos.core.annotation.Config;
import org.jpos.q2.QFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigAnnotationTest {
    @Test
    public void testConfig() throws ConfigurationException, IllegalAccessException {
        MyAutoConfigurable bean = new MyAutoConfigurable();
        Configuration cfg = new SimpleConfiguration();
        cfg.put("mystring", "My String");
        cfg.put("mylong", "10000");
        cfg.put("myint", "1000");
        cfg.put("myboolean", "yes");
        QFactory.autoconfigure(bean, cfg);
        assertEquals("My String", bean.getMystring());
        assertEquals(1000, bean.getMyint());
        assertEquals(10000L, bean.getMylong());
        assertTrue(bean.isMyboolean());
    }

    @Test
    public void testChildConfig() throws ConfigurationException, IllegalAccessException {
        MyChildAutoConfigurable bean = new MyChildAutoConfigurable();
        Configuration cfg = new SimpleConfiguration();
        cfg.put("mystring", "My String");
        cfg.put("mylong", "10000");
        cfg.put("myint", "1000");
        cfg.put("myboolean", "yes");
        cfg.put("mychildstring", "My Child String");
        QFactory.autoconfigure(bean, cfg);
        assertEquals("My String", bean.getMystring());
        assertEquals(1000, bean.getMyint());
        assertEquals(10000L, bean.getMylong());
        assertEquals("My Child String", bean.getChildString());
        assertTrue(bean.isMyboolean());
    }


    public static class MyAutoConfigurable {
        @Config("mystring")
        private String mystring;

        @Config("myint")
        private int myint;

        @Config("mylong")
        private Long mylong;

        @Config("myboolean")
        private boolean myboolean;

        public String getMystring() {
            return mystring;
        }

        public int getMyint() {
            return myint;
        }

        public Long getMylong() {
            return mylong;
        }

        public boolean isMyboolean() {return myboolean; }


        @Override
        public String toString() {
            return "MyAutoConfigurable{" +
              "mystring='" + mystring + '\'' +
              ", myint=" + myint +
              ", mylong=" + mylong +
              '}';
        }
    }

    public static class MyChildAutoConfigurable extends MyAutoConfigurable {
        @Config("mychildstring")
        private String childString;

        public String getChildString() {
            return childString;
        }
    }

}
