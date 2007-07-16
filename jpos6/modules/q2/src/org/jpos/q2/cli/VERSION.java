package org.jpos.q2.cli;

import org.jpos.q2.CLI;

public class VERSION implements CLI.Command {
    public void exec (CLI cli, String[] args) {
        cli.println ("jPOS Q2 " + cli.getQ2().Q2_VERSION);
    }
}

