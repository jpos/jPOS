package org.jpos.q2.cli;

import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.CLISubSystem;
import org.jpos.security.jceadapter.JCESecurityModule;

import java.util.HashMap;
import java.util.Map;

/**
 * CLI implementation - SSM subsystem
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class SSM implements CLISubSystem, CLICommand {
    private static final String SYSTEM_KEY = "SSM";
    private static final String JCE_KEY = "jce-sm";

    @Override
    public String getPrompt(CLIContext ctx, String[] args) {
        return "ssm> ";
    }

    @Override
    public String[] getCompletionPrefixes(CLIContext ctx, String[] args) {
        return new String[] { "org.jpos.q2.cli.ssm.actions." };
    }

    @Override
    public void exec(CLIContext cli, String[] strings) throws Exception {
        cli.setActiveSubSystem(SYSTEM_KEY);
        cli.getUserData().put(SYSTEM_KEY, new HashMap<String, Object>());
    }

    public static JCESecurityModule getSecurityModule(CLIContext cliContext) {
        return (JCESecurityModule) getSystemStorage(cliContext).get(JCE_KEY);
    }

    public static void setSecurityModule(CLIContext cliContext, JCESecurityModule securityModule) {
        getSystemStorage(cliContext).put(JCE_KEY, securityModule);
    }

    private static Map<String, Object> getSystemStorage(CLIContext cliContext) {
        return (Map<String, Object>) cliContext.getUserData().get(SYSTEM_KEY);
    }
}
