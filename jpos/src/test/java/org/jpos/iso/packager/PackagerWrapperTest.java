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

package org.jpos.iso.packager;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PackagerWrapperTest {
    private static boolean wrongTypeInitialized;
    private static boolean wrongTypeConstructed;

    public static class WrongInnerPackagerType {
        static {
            wrongTypeInitialized = true;
        }

        public WrongInnerPackagerType() {
            wrongTypeConstructed = true;
        }
    }

    @Test
    public void testRejectsWrongInnerPackagerTypeBeforeInitialization() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("inner-packager", PackagerWrapperTest.class.getName() + "$WrongInnerPackagerType");

        assertThrows(ConfigurationException.class, () -> new TestWrapper().setConfiguration(cfg));
        assertFalse(wrongTypeInitialized);
        assertFalse(wrongTypeConstructed);
    }

    @Test
    public void testAcceptsValidInnerPackagerType() throws ConfigurationException {
        SimpleConfiguration cfg = new SimpleConfiguration();
        cfg.put("inner-packager", ISO87APackager.class.getName());
        TestWrapper wrapper = new TestWrapper();

        wrapper.setConfiguration(cfg);

        assertInstanceOf(ISO87APackager.class, wrapper.getPackager());
    }

    private static class TestWrapper extends PackagerWrapper {
        @Override
        public byte[] pack(ISOComponent c) throws ISOException {
            return standardPackager.pack(c);
        }

        @Override
        public int unpack(ISOComponent c, byte[] b) throws ISOException {
            return standardPackager.unpack(c, b);
        }

        @Override
        public void unpack(ISOComponent c, InputStream in) throws IOException, ISOException {
            standardPackager.unpack(c, in);
        }

        @Override
        public String getDescription() {
            return standardPackager.getDescription();
        }

        @Override
        public ISOMsg createISOMsg() {
            return standardPackager.createISOMsg();
        }
    }
}
