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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChronometerTest {
    // Generous tolerance to absorb JVM cold-start and OS scheduler jitter,
    // especially when running the full test suite under load.
    // A 1-second ceiling on a 25 ms sleep still reliably catches bugs where
    // elapsed() returns 0, wraps, measures in wrong units, etc.
    private long TOLERANCE = 1000L;

    /**
     * Prime the JVM timer infrastructure before any timing-sensitive tests run.
     * Without this, the very first Thread.sleep() can measure several hundred
     * milliseconds due to JVM class-loading and timer-thread initialisation,
     * causing spurious failures in the assertions below.
     */
    @BeforeAll
    static void warmUp() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            Chronometer w = new Chronometer();
            Thread.sleep(20);
            w.elapsed();
            w.lap();
        }
    }

    @Test
    public void testElapsed() throws InterruptedException {
        Chronometer c = new Chronometer();
        Thread.sleep(25);
        assertTrue(approx (25L, c.elapsed()), "out-of-range " + c.elapsed());
        Thread.sleep(25);
        assertTrue(approx (50L, c.elapsed()), "out-of-range " + c.elapsed());
    }

    @Test
    public void testLap() throws InterruptedException {
        Chronometer c = new Chronometer();
        Thread.sleep(25);
        long lap = c.lap();
        assertTrue(approx (25L, lap), "out-of-range " + lap);
        Thread.sleep(25);
        assertTrue(approx (25L, lap), "out-of-range " + lap);
        Thread.sleep(25);
        assertTrue(approx (75L, c.elapsed()), "out-of-range " + c.elapsed());
    }

    private boolean approx(long expected, long value) {
        return value >= expected && value < expected + TOLERANCE;
    }
}
