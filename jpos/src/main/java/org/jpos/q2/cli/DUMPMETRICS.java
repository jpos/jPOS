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
import org.jpos.util.Metrics;
import org.jpos.util.MetricsProvider;
import org.jpos.util.NameRegistrar;

import java.io.File;
import java.io.IOException;

public class DUMPMETRICS implements CLICommand {
    public void exec(CLIContext ctx, String[] args) throws IOException {
        if (args.length != 2) {
            ctx.println (String.format ("Usage: %s<path-to-dump-directory>", args[0]));
            return;
        }
        File dir = new File(args[1]);
        if (!dir.canWrite()) {
            ctx.println ("Unable to write " + dir.getAbsolutePath());
            return;
        }
        dumpMetrics (dir);
    }

    private void dumpMetrics(File dir) {
        NameRegistrar.getAsMap().forEach((key, value) -> {
            if (value instanceof MetricsProvider) {
                Metrics metrics = ((MetricsProvider) value).getMetrics();
                metrics.dumpHistograms(dir, key + "-");
            }
        });
    }
}
