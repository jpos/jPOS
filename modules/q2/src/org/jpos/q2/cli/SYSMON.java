package org.jpos.q2.cli;

import org.jpos.util.SystemMonitor;
import org.jpos.q2.CLI;

public class SYSMON implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        new SystemMonitor().dump (cli.getOutputStream(), " ");
    }
}

