package org.jpos.q2.cli.builtin;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;

/**
 * Prints the contents of the CLIContext user data.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class USERDATA implements CLICommand {
    @Override
    public void exec(CLIContext cli, String[] strings) throws Exception {
        cli.printUserData();
    }
}
