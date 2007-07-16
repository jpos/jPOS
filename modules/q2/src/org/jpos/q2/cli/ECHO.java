package org.jpos.q2.cli;

import org.jpos.q2.CLI;

public class ECHO implements CLI.Command {
    public void exec (CLI cli, String[] args) {
        String s = args[0].substring(4);
        if (s.length() > 1)
            s = s.substring(2);
        cli.println (s);
    }
}

