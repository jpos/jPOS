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

package org.jpos.q2.cli.ssm.actions;

import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.q2.CLICommand;
import org.jpos.q2.CLIContext;
import org.jpos.q2.cli.SSM;
import org.jpos.security.jceadapter.JCESecurityModule;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.PrintStream;
import java.util.Properties;

/**
 * Initialize {@link JCESecurityModule} and stores it in the context of the ssm subsystem.
 *
 * @author Alwyn Schoeman - alwyn.schoeman@gmail.com
 */
public class INIT implements CLICommand {

    @Override
    public void exec(CLIContext cli, String[] strings) throws Exception {
        int numArgs = strings.length;
        if (numArgs == 1) {
            cli.println("Usage: init -lmk filename -rebuildlmk -jce <provider class name>");
            return;
        }
        Properties cfgProps = new Properties();
        SimpleConfiguration cfg = new SimpleConfiguration(cfgProps);
        int curArg = 1; // First string is command name
        while (curArg <= numArgs) {
            if (curArg < numArgs && strings[curArg].compareToIgnoreCase("-lmk") == 0) {
                curArg++;
                cfgProps.setProperty("lmk", strings[curArg++]);
            }
            if (curArg < numArgs && strings[curArg].compareToIgnoreCase("-jce") == 0) {
                curArg++;
                cfgProps.setProperty("provider", strings[curArg++]);
            }
            if (curArg < numArgs && strings[curArg].compareToIgnoreCase("-rebuildlmk") == 0) {
                cfgProps.setProperty("rebuildlmk", "true");
                curArg++;
            }
            curArg++;
        }
        JCESecurityModule sm = new JCESecurityModule();
        Logger logger = new Logger();
        logger.addListener(new SimpleLogListener(new PrintStream(cli.getOutputStream())));
        sm.setLogger(logger, "jce-security-module");
        try {
            sm.setConfiguration(cfg);
            SSM.setSecurityModule(cli, sm);
        } catch (ConfigurationException e) {
            cli.printThrowable(e);
        }
    }
}
