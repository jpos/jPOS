package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.Profiler;

import java.util.concurrent.CountDownLatch;

@SuppressWarnings("unused")
public class LOGGER_BENCHMARK implements CLICommand {
    public void exec(CLIContext ctx, String[] args) throws Exception {
        if (args.length != 3) {
            ctx.println (String.format ("Usage: %s threads messages", args[0]));
            return;
        }
        int threadCount = Integer.parseInt(args[1]);
        final int numMessages = Integer.parseInt(args[2]);
        final Profiler p = new Profiler();
        final CountDownLatch done = new CountDownLatch(threadCount);
        for (int i = 0; i<threadCount; i++) {
            final  String name = "Thread " + i;
            new Thread() {
                public void run() {
                    for (int i = 0; i < numMessages; i++) {
                        LogEvent ev = new LogEvent();
                        ev.addMessage(name + " " + i);
                        Logger.log(ev);
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
