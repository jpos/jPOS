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

package org.jpos.log.evt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;
import org.jpos.util.Profiler;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Structured representation of a {@link Profiler} trace.
 *
 * <p>Each checkpoint is emitted as a 3-element tuple
 * {@code [name, durationNs, totalNs]} to keep the on-wire footprint small —
 * Profiler traces appear on every transaction, so per-checkpoint field
 * names would dominate the log volume. Durations are in nanoseconds.</p>
 *
 * <p>Example payload:</p>
 * <pre>{@code
 * {"t":"profiler","elapsed_ns":4885417,"checkpoints":[
 *   ["prepare:pre-retry", 9333, 9333],
 *   ["commit:pre-retry", 2656917, 2666250],
 *   ["end", 2219167, 4885417]
 * ]}
 * }</pre>
 *
 * @param elapsedNs   total elapsed nanoseconds (from the {@code end} checkpoint)
 * @param checkpoints ordered list of {@code [name, durationNs, totalNs]} tuples
 *
 * @since 3.0.2
 */
@JacksonXmlRootElement(localName = "profiler")
public record ProfilerEvt(
    @JsonProperty("elapsed_ns") @JacksonXmlProperty(isAttribute = true) long elapsedNs,
    @JsonProperty("checkpoints") List<Object[]> checkpoints
) implements AuditLogEvent {

    /**
     * Builds a {@code ProfilerEvt} from a {@link Profiler}. Mirrors
     * {@link Profiler#dump} by ensuring an {@code end} checkpoint is present, so
     * the elapsed time and final entry match the legacy text rendering.
     *
     * @param p the profiler to snapshot
     * @return a structured snapshot
     */
    public static ProfilerEvt of(Profiler p) {
        if (p.getEntry("end") == null) {
            p.checkPoint("end");
        }
        LinkedHashMap<String, Profiler.Entry> events = p.getEvents();
        List<Object[]> tuples = events.entrySet().stream()
            .map(e -> new Object[] {
                e.getKey(),
                e.getValue().getDurationInNanos(),
                e.getValue().getTotalDurationInNanos()
            })
            .toList();
        Profiler.Entry end = events.get("end");
        long elapsed = end != null ? end.getTotalDurationInNanos() : 0L;
        return new ProfilerEvt(elapsed, tuples);
    }
}
