package org.jpos.q2.cli;

import java.util.Date;
import java.io.IOException;
import org.jpos.q2.CLI;

public class DATE implements CLI.Command {
    public void exec (CLI cli, String[] args) {
        cli.println (new Date().toString());
    }
}

