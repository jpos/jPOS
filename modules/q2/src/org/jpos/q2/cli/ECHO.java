package org.jpos.q2.cli;

import org.jpos.iso.ISOUtil;
import org.jpos.q2.CLI;

public class ECHO implements CLI.Command {
    public void exec (CLI cli, String[] args) {
        String s = ISOUtil.unPadLeft(args[0].substring(4), ' ');
        cli.println (s);
    }
}

