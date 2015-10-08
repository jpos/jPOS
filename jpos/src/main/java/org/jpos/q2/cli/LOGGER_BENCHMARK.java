package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.Profiler;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class LOGGER_BENCHMARK implements CLICommand, Runnable {
    static AtomicInteger threadNumber = new AtomicInteger();
    public void exec(CLIContext ctx, String[] args) throws Exception {
        if (args.length != 2) {
            ctx.println ("Usage: " + args[0] + "threads");
            return;
        }
        int threads = Integer.parseInt(args[1]);
        for (int i = 0; i<threads; i++) {
            new Thread(this).start();
        }
    }

    public void run() {
        String name = "Thread " + threadNumber.incrementAndGet() + " ";
        Profiler p = new Profiler();
        for (int i = 0; i <= 1000000; i++) {
            LogEvent ev = new LogEvent();
            ev.addMessage(name + i);
            Logger.log(ev);
        }
        p.dump(System.out, name);
    }
}
