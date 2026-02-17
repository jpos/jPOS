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

package org.jpos.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class TPSTestCase {
    @Test
    public void test1000TPSAutoUpdate() throws Exception {
        TPS tps = new TPS(true);
        Instant nowInit = Instant.now();
        for (int i=0; i<1000; i++)
            tps.tick();
        Instant nowDone = Instant.now();
        sleepAtLeast(1100L - Duration.between(nowInit, Instant.now()).toMillis()); // scheduler is not perfectly accurate
        assertInRange(tps.intValue(), 950, 1050, "Expected around 1000 TPS");
        assertInRange(tps.intValue(), 950, 1050, "Still expecting around 1000 TPS on a second call");
        sleepAtLeast(2100L - Duration.between(nowDone, Instant.now()).toMillis());
        assertTrue(tps.getAvg() >= 0.5, "Average should be aprox 0.5 but it's " + tps.getAvg());
        sleepAtLeast(3100L - Duration.between(nowDone, Instant.now()).toMillis());
        assertEquals(
            0, tps.intValue(),
            "TPS should be zero but it's " + tps.intValue() + " (" + tps.floatValue() + ")"
        );
        assertInRange(tps.getPeak(), 950, 1050, "Peak should be around 1000");
        tps.stop();
    }
    @Test
    public void test1000TPSManualUpdate() throws Exception {
        TPS tps = new TPS();
        Instant nowInit = Instant.now();
        for (int i=0; i<1000; i++)
            tps.tick();
        Instant nowDone = Instant.now();
        sleepAtLeast(1050L - Duration.between(nowInit, Instant.now()).toMillis());
        assertTrue(tps.intValue() >= 800, "Expected aprox 1000 TPS but was " + tps.intValue());
        assertTrue(tps.intValue() >= 800, "Still expecting aprox 1000 TPS on a second call");
        sleepAtLeast(2500L - Duration.between(nowDone, Instant.now()).toMillis());
        assertEquals(
            0, tps.intValue(),
            "TPS should be zero but it's " + tps.intValue() + " (" + tps.floatValue() + ")"
        );
    }

    @Test
    public void testManualUpdateWithSimulatedTime() {
        AtomicLong now = new AtomicLong(1_000_000_000L);
        TPS tps = new TPS(1000L, false, now::get);

        for (int i = 0; i < 1000; i++)
            tps.tick();

        now.addAndGet(1_000_000_000L); // +1s
        assertEquals(1000, tps.intValue(), "Expected exact 1000 TPS with simulated time");

        now.addAndGet(1_000_000_000L); // +1s with no ticks
        assertEquals(0, tps.intValue(), "Expected 0 TPS after idle interval");
    }

    private static void sleepAtLeast(long millis) throws InterruptedException {
        if (millis > 0L) {
            Thread.sleep(millis);
        }
    }

    private static void assertInRange(int value, int minInclusive, int maxInclusive, String message) {
        assertTrue(value >= minInclusive && value <= maxInclusive,
          message + " (actual=" + value + ")");
    }
}
