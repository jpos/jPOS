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

import java.io.PrintStream;
import java.io.Serializable;

/**
 * {@link LogEvent} that captures a precomputed Markdown rendering and replays
 * it verbatim on subsequent {@code dump}/{@code toString} calls.
 */
public class MarkdownLogEvent extends LogEvent {
    private String frozen;

    /**
     * Constructs a MarkdownLogEvent from a precomputed Markdown string.
     *
     * @param frozen the Markdown text to render
     */
    public MarkdownLogEvent(String frozen) {
        this.frozen = frozen;
    }
    /**
     * Constructs a MarkdownLogEvent by capturing {@code evt}'s rendered form.
     *
     * @param evt source event whose rendered text is captured
     */
    public MarkdownLogEvent (LogEvent evt) {
        super(evt.getSource(), evt.getTag(), evt.getRealm());
        frozen = evt.toString();
    }
    @Override
    public void dump (PrintStream ps, String indent) {
        ps.print (frozen);
    }

    @Override
    public String toString () {
        return frozen;
    }
}

