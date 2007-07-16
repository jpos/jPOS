package org.jpos.q2.cli;

import java.io.IOException;
import org.jpos.q2.CLI;

public class SHUTDOWN implements CLI.Command {
    public void exec (CLI cli, String[] args) throws IOException {
        boolean shutdown = false;
        if (args.length == 2 && "--force".equals (args[1]))
            shutdown = true;
        else 
            shutdown = cli.confirm ("Confirm shutdown (Yes/No) ? ");

        if (shutdown) {
            cli.println ("Shutting down.");
            cli.getQ2().shutdown();
        }
        else 
            cli.println ("Q2 will continue running.");
    }
}

