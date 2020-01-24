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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.function.RemoveNewLinesMapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A LogListener that uses the RemoveNewLinesMapper to remove newlines from the LogEvent dump output.
 *
 * This LogListener will modify the LogEvents for all subsequent LogListeners in the configuration.
 * If this is not what you want, order your LogListeners to minimize the effect.
 *
 * @see RemoveNewLinesMapper RemoveNewLinesMapper for more details.
 * @author Alejandro P. Revilla
 * @author Alwyn Schoeman
 * @since 2.1.4
 */
public class FlatLogListener implements LogListener, Configurable, Destroyable {
    RemoveNewLinesMapper mapper = new RemoveNewLinesMapper();
    ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
    PrintStream p = new PrintStream(captureStream);

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        mapper.setConfiguration(cfg);
    }

    @Override
    public synchronized LogEvent log(LogEvent ev) {
        ev.dump(p, "");
        byte[] result = mapper.apply(captureStream.toByteArray());
        captureStream.reset();
        return new FrozenLogEvent(new String(result));
    }

    @Override
    public void destroy() {
        if (p != null) {
            p.close();
            p = null;
            captureStream = null;
        }
    }
}
