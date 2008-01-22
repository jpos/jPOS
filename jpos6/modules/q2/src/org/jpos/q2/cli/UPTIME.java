/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2008 Alejandro P. Revilla
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

import java.util.Date;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOException;
import org.jpos.q2.CLI;

public class UPTIME implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        StringBuffer sb = new StringBuffer();
        long uptime = cli.getQ2().getUptime();
        int ms = (int) (uptime % 1000);
        uptime /= 1000;
        int dd = (int) (uptime/86400);
        uptime -= (dd * 86400);
        int hh = (int) (uptime/3600);
        uptime -= (hh * 3600);
        int mm = (int) (uptime/60);
        uptime -= (mm * 60);
        int ss = (int) uptime;
        if (dd > 0) {
            sb.append (Long.toString(dd));
            sb.append ("d ");
        }
        sb.append (zeropad (hh, 2));
        sb.append (':');
        sb.append (zeropad (mm, 2));
        sb.append (':');
        sb.append (zeropad (ss, 2));
        sb.append ('.');
        sb.append (zeropad (ms, 3));
        cli.println (sb.toString());
    }
    private String zeropad (int i, int l) throws ISOException {
        return ISOUtil.zeropad (Integer.toString (i), l);
    }
}

