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

package org.jpos.transaction.participant;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import static org.jpos.transaction.ContextConstants.TXNNAME;

/**
 * {@link GroupSelector} that picks the next participant group based on the
 * transaction name stored in the context (default key {@link
 * org.jpos.transaction.ContextConstants#TXNNAME}).
 * When configured with {@code mode=prefix}, the traditional exact match is attempted
 * first, followed by the longest configured key that prefixes the transaction name.
 */
@SuppressWarnings("unused")
public class Switch implements Configurable, GroupSelector {
    private static final String PREFIX_MODE = "prefix";
    private static final Set<String> SELECTOR_PROPERTIES = Set.of("mode", "txnname", "unknown");

    private Configuration cfg;
    private String txnNameEntry;
    private boolean prefixMode;
    private List<String> sortedRoutingKeys = List.of();   // kept sorted from longest to shortest

    /** Creates the selector; configuration is supplied via {@link #setConfiguration(Configuration)}. */
    public Switch() {}

    public String select (long id, Serializable ser) {
        Context ctx   = (Context) ser;
        String type   = ctx.getString (txnNameEntry);
        String groups = null;

        if (type != null) {
            groups = cfg.get (type, null);

            if (groups == null && prefixMode) {
                String matchedKey = sortedRoutingKeys.stream()
                  .filter(type::startsWith)
                  .findFirst()
                  .orElse(null);

                if (matchedKey != null) {
                    groups = cfg.get (matchedKey, null);
                }
            }
        }

        if (groups == null)
            groups = cfg.get ("unknown", "");
        ctx.log ("SWITCH " + type + " (" + groups + ")");

        return groups;
    }

    public int prepare (long id, Serializable o) {
        return PREPARED | READONLY | NO_JOIN;
    }

    /**
     * Configures the selector. Recognized properties:
     * <ul>
     *   <li>{@code txnname} - context key holding the transaction name (default `TXNNAME` from {@link
     *       org.jpos.transaction.ContextConstants#TXNNAME}).</li>
     *   <li>{@code mode} - {@code strict} (default) for exact match only, or {@code prefix}
     *       to fall back to the longest configured key that prefixes the transaction name.</li>
     *   <li>{@code unknown} - group(s) returned when no match is found.</li>
     *   <li>any other key - transaction name (or prefix, in {@code prefix} mode) mapped to
     *       the group(s) to select for it.</li>
     * </ul>
     */
    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
        txnNameEntry = cfg.get("txnname", TXNNAME.toString());
        prefixMode = PREFIX_MODE.equals(cfg.get("mode", "strict"));
        if (prefixMode)
            sortedRoutingKeys = cfg.keySet().stream()
              .filter(key -> !SELECTOR_PROPERTIES.contains(key))
              .sorted(Comparator.comparingInt(String::length).reversed())
              .toList();
    }
}
