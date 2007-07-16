package org.jpos.q2.cli;

import java.util.Date;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOException;
import org.jpos.q2.CLI;

public class CLR implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        cli.getConsoleReader().clearScreen();
    }
}

