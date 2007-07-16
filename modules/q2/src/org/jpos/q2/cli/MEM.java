package org.jpos.q2.cli;

import org.jpos.util.SystemMonitor;
import org.jpos.q2.CLI;

public class MEM implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        if (args.length > 1 && "--gc".equals (args[1])) {
            System.gc();
        }
        Runtime r = Runtime.getRuntime();
        StringBuffer sb = new StringBuffer();
        sb.append ("total="); sb.append (r.totalMemory());
        sb.append (" free="); sb.append (r.freeMemory());
        sb.append (" in-use="); sb.append (r.totalMemory()-r.freeMemory());
        cli.println (sb.toString());
    }
}

