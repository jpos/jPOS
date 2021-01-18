/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2021 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
