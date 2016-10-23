package org.jpos.q2.cli;

import org.jpos.q2.CLIContext;
import org.jpos.q2.CLISubSystem;

/**
 * CLI implementation - Deploy subsystem
 * 
 * @author Felipph Calado - luizfelipph@gmail.com
 */
public class DEPLOY implements CLISubSystem {
    @Override
    public String getPrompt(CLIContext ctx, String[] args) {
        return "deploy> ";
    }

    @Override
    public String[] getCompletionPrefixes(CLIContext ctx, String[] args) {
        return new String[] { "org.jpos.q2.cli.deploy." };
    }
}
