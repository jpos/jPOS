/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2015 Alejandro P. Revilla
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

import org.jpos.iso.ISODate;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class TZCHECK implements CLICommand
{
    public void exec(CLIContext cli, String[] args) throws Exception
    {
        TimeZone tz = TimeZone.getDefault();
        cli.println(
            "TimeZone ID: " + tz.getID() + " (" + tz.getDisplayName() + ")"
        );
        cli.println("Date: " + new Date());
        cli.println("System: " + System.currentTimeMillis());
        long now = System.currentTimeMillis();
        int offset = tz.getRawOffset();
        boolean offsetChange = false;
        for (int i=0; i<366; i++) {
            now += 86400000L;
            if (tz.getOffset(now) != offset) {
                Date d = new Date(now);
                cli.println ("GMT offset will change from " + offset/3600000
                        + " to " + tz.getOffset(now)/3600000
                        + " on " + ISODate.formatDate(d, "yyyyMMdd"));
                offsetChange = true;
                offset = tz.getOffset(now);
            }
        }
        if (!offsetChange)
            cli.println ("GMT offset won't change during the next year");
    }
}
