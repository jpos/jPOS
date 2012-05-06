/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

package org.jpos.util;

import junit.framework.TestCase;

public class TPSTestCase extends TestCase {
    public void test1000TPSAutoUpdate() throws Exception {
        TPS tps = new TPS(true);
        for (int i=0; i<1000; i++)
            tps.tick();
        Thread.sleep (1050L); // java.util.Timer is not accurate
        assertEquals("Expected 1000 TPS", 1000, tps.intValue());
        assertEquals("Still expecting 1000 TPS on a second call", 1000, tps.intValue());
        Thread.sleep (1000L);
        assertTrue ("Average should be aprox 0.5 but it's " + tps.getAvg(), tps.getAvg() >= 0.5);
        assertEquals(
            "TPS should be zero but it's "+tps.intValue() + " (" + tps.floatValue() + ")",
            0, tps.intValue()
        );
        assertEquals ("Peak has to be 1000", 1000, tps.getPeak());
        tps.stop();
    }
    public void test1000TPSManualUpdate() throws Exception {
        TPS tps = new TPS();
        for (int i=0; i<1000; i++)
            tps.tick();
        Thread.sleep (1050L);
        assertTrue("Expected aprox 1000 TPS but was "+ tps.intValue(), tps.intValue() >= 900);
        assertTrue("Still expecting aprox 1000 TPS on a second call", tps.intValue() >= 900);
        Thread.sleep (1050L);
        assertEquals(
            "TPS should be zero but it's "+tps.intValue() + " (" + tps.floatValue() + ")",
            0, tps.intValue()
        );
    }
}

