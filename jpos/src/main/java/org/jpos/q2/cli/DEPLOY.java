package org.jpos.q2.cli;

import org.jpos.q2.CLISubSystem;

/**
 * CLI implementation - Deploy subsystem
 * 
 * @author Felipph Calado - luizfelipph@gmail.com
 */
public class DEPLOY implements CLISubSystem {
    @Override
    public String getPrompt(String[] args) {
        return "deploy> ";
    }

    @Override
    public String[] getCompletionPrefixes(String[] args) {
        return new String[] { "org.jpos.q2.cli.deploy." };
    }

}
