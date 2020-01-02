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

package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class TZCHECK implements CLICommand
{
    public void exec(CLIContext cli, String[] args) throws Exception
    {
        ZoneId zi = ZoneId.systemDefault();
        Instant i = Instant.now();
        cli.println(
            "         Zone ID: " + zi + " (" + zi.getDisplayName(TextStyle.FULL, Locale.getDefault()) + ") "
                + zi.getRules().getOffset(i)
        );
        cli.println ("             UTC: " + i);
        ZoneOffsetTransition tran = zi.getRules().nextTransition(i);
        if (tran != null) {
            Instant in = tran.getInstant();
            cli.println (" Next transition: " + in + " (" + in.atZone(zi) + ")");
        }
        List<ZoneOffsetTransitionRule> l = zi.getRules().getTransitionRules();
        for (ZoneOffsetTransitionRule r : l) {
            cli.println (" Transition rule: " + r);
        }
    }
}
