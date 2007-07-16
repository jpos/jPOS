package org.jpos.q2.cli;

import org.jpos.q2.CLI;

public class HELP implements CLI.Command {
    public void exec (CLI cli, String[] args) {
        cli.println ("Type tab to see list of available commands");
        cli.println ("Type 'man command-name' to see man page");
    }
}

