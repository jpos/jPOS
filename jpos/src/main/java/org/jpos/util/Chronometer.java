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

import java.time.Duration;
import java.time.Instant;

public class Chronometer {
    private Instant start;
    private Instant lap;

    public Chronometer() {
        this.start = Instant.now();
        this.lap = this.start;
    }

    public long elapsed() {
        return Duration.between(Instant.now(), start).toMillis();
    }

    public void reset () {
        start = Instant.now();
    }

    public long lap() {
        Instant now = Instant.now();
        long elapsed = Duration.between(now, lap).toMillis();
        lap = now;
        return elapsed;
    }

    @Override
    public String toString() {
        return "Chronometer{" +
          "elapsed=" + elapsed() +
          ", lap=" + Duration.between(Instant.now(), lap).toMillis() +
          '}';
    }
}

