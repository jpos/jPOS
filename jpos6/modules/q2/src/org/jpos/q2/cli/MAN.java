package org.jpos.q2.cli;

import java.io.InputStream;
import org.jpos.q2.CLI;

public class MAN implements CLI.Command {
    public void exec (CLI cli, String[] args) throws Exception {
        if (args.length < 2) {
            cli.println ("What manual page do you want?");
            return;
        }
        String command = args[1];
        InputStream is = MAN.class.getResourceAsStream(
            command.toUpperCase()+".man"
        );
        if (is != null) {
            byte[] b = new byte[is.available()];
            is.read (b);
            cli.print (new String(b, "ISO8859_1"));
        } else {
            cli.println ("No manual entry for " + command);
        }
    }
}

