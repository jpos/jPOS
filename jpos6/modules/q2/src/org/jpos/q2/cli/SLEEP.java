package org.jpos.q2.cli;

import org.jpos.q2.CLI;

public class SLEEP implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        if (args.length > 1) {
            Thread.sleep (Long.parseLong (args[1])*1000);
        } else {
            cli.println ("Usage: sleep number-of-seconds");
        }
    }
}

