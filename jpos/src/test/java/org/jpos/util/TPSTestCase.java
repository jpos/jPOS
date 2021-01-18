/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
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

import java.time.Duration;
import java.time.Instant;

public class TPSTestCase {
    @Test
    public void test1000TPSAutoUpdate() throws Exception {
        TPS tps = new TPS(true);
        Instant nowInit = Instant.now();
        for (int i=0; i<1000; i++)
            tps.tick();
        Instant nowDone = Instant.now();
        Thread.sleep (1100L - Duration.between(nowInit, Instant.now()).toMillis()); // java.util.Timer is not accurate
        assertEquals(1000, tps.intValue(), "Expected 1000 TPS");
        assertEquals(1000, tps.intValue(), "Still expecting 1000 TPS on a second call");
        Thread.sleep (2100L - Duration.between(nowDone, Instant.now()).toMillis());
        assertTrue(tps.getAvg() >= 0.5, "Average should be aprox 0.5 but it's " + tps.getAvg());
        Thread.sleep (3100L - Duration.between(nowDone, Instant.now()).toMillis());
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
        Instant nowInit = Instant.now();
        for (int i=0; i<1000; i++)
            tps.tick();
        Instant nowDone = Instant.now();
        Thread.sleep (1050L - Duration.between(nowInit, Instant.now()).toMillis());
        assertTrue(tps.intValue() >= 800, "Expected aprox 1000 TPS but was " + tps.intValue());
        assertTrue(tps.intValue() >= 800, "Still expecting aprox 1000 TPS on a second call");
        Thread.sleep (2500L - Duration.between(nowDone, Instant.now()).toMillis());
        assertEquals(
            0, tps.intValue(),
            "TPS should be zero but it's " + tps.intValue() + " (" + tps.floatValue() + ")"
        );
    }
}

