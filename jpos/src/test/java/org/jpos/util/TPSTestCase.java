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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TPSTestCase {
    @Test
    public void test1000TPSAutoUpdate() throws Exception {
        TPS tps = new TPS(true);
        for (int i=0; i<1000; i++)
            tps.tick();
        Thread.sleep (1050L); // java.util.Timer is not accurate
        assertEquals(1000, tps.intValue(), "Expected 1000 TPS");
        assertEquals(1000, tps.intValue(), "Still expecting 1000 TPS on a second call");
        Thread.sleep (1000L);
        assertTrue(tps.getAvg() >= 0.5, "Average should be aprox 0.5 but it's " + tps.getAvg());
        assertEquals(
            0, tps.intValue(),
            "TPS should be zero but it's " + tps.intValue() + " (" + tps.floatValue() + ")"
        );
        assertEquals(1000, tps.getPeak(), "Peak has to be 1000");
        tps.stop();
    }
    @Test
    public void test1000TPSManualUpdate() throws Exception {
        TPS tps = new TPS();
        for (int i=0; i<1000; i++)
            tps.tick();
        Thread.sleep (1050L);
        assertTrue(tps.intValue() >= 900, "Expected aprox 1000 TPS but was " + tps.intValue());
        assertTrue(tps.intValue() >= 900, "Still expecting aprox 1000 TPS on a second call");
        Thread.sleep (1050L);
        assertEquals(
            0, tps.intValue(),
            "TPS should be zero but it's " + tps.intValue() + " (" + tps.floatValue() + ")"
        );
    }
}

