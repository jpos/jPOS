/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2016 Alejandro P. Revilla
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
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.util.Profiler;

import java.util.concurrent.CountDownLatch;

@SuppressWarnings("unused")
public class NEXTLONG_BENCHMARK implements CLICommand {
    public void exec(CLIContext ctx, String[] args) throws Exception {
        if (args.length != 4) {
            ctx.println (String.format ("Usage: %sspacename threads iterations", args[0]));
            return;
        }
        final Space sp = SpaceFactory.getSpace(args[1]);
        int threadCount = Integer.parseInt(args[2]);
        int iterations = Integer.parseInt(args[3]);
        final Profiler p = new Profiler();
        final CountDownLatch done = new CountDownLatch(threadCount);
        for (int i = 0; i<threadCount; i++) {
            final String name = "Thread " + i;
            new Thread() {
                public void run() {
                    for (int i = 0; i < iterations; i++) {
                        SpaceUtil.nextLong (sp, "CNT");
                    }
                    p.checkPoint (name);
                    done.countDown();

                }
            }.start();
        }
        done.await();
        p.dump (System.out, "");
    }
}
