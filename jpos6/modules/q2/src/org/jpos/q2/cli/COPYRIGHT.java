package org.jpos.q2.cli;

import java.io.InputStream;
import java.io.IOException;
import org.jpos.q2.CLI;

public class COPYRIGHT implements CLI.Command {
    public void exec (CLI cli, String[] args) throws IOException {
        display (cli, MAN.class.getResourceAsStream("/COPYRIGHT"));
        cli.println ("");
    }
    private void display (CLI cli, InputStream is) throws IOException {
        if (is != null) {
            while (is.available() > 0) {
                byte[] b = new byte[is.available()];
                is.read (b);
                cli.print (new String(b, "ISO8859_1"));
            }
        }
    }
}

