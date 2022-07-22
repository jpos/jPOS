/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2010 Alejandro P. Revilla
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChronometerTest {
    private long TOLERANCE = 200L;
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
