/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
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

package org.jpos.q2.iso;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;

/**
 * A class that can be added to a {@link MUXPool} to override the classical built-in strategies.<br>
 *
 * It could be added to a {@code MUXPool} like this:<br>
 *
 * <pre>
 *    &lt;mux class="org.jpos.q2.iso.MUXPool" logger="Q2" name="my-pool">
 *      &lt;muxes>mux1 mux2 mux3&lt;/muxes>
 *      &lt;strategy>round-robin&lt;/strategy>
 *
 *      &lt;strategy-handler class="xxx.yyy.MyPoolStrategy">
 *        &lt;!-- some config here --&gt;
 *      &lt;/strategy-handler>
 *    &lt;/mux>
 * </pre>
 *
 * If the {@code strategy-handler} returns {@code null}, the {@link MUXPool} will fall back to the
 * defined {@code strategy} (or the default one, if none defined).
 *
 * @author barspi@transactility.com
 */
public interface MUXPoolStrategyHandler {
    /** If this method returns null, the {@link MUXPool} will fall back to the configured built-in
     *  strategy.
     *
     * @param m the {@link ISOMsg} that we wish to send
     * @param maxWait deadline in milliseconds (epoch value as given by {@code System.currentTimeMillis()})
     * @param mp the {@link MUXPool} using this strategy handler
     * @return an appropriate {@link MUX} for this strategy, or {@code null} if none is found
     */
    MUX getMUX(ISOMsg m, long maxWait, MUXPool mp);
}
