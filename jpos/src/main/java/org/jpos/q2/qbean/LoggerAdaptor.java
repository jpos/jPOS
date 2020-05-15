/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

package org.jpos.q2.qbean;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.LogEventOutputStream;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;

import java.io.IOException;
import java.io.PrintStream;

public class LoggerAdaptor extends QBeanSupport {
    private Logger logger;
    private PrintStream originalOut = null;
    private PrintStream originalErr = null;

    protected void initService () {
        logger = Logger.getLogger (getName());
    }
    protected void startService () throws ConfigurationException, IOException {
        logger.removeAllListeners ();
        for (Object o : getPersist().getChildren("log-listener"))
            addListener((Element) o);

        String redirect = cfg.get("redirect");
        long delay = cfg.getLong("delay", 500);

        if (redirect.contains("stdout")) {
            originalOut = System.out;
            System.setOut(new PrintStream(new LogEventOutputStream(logger, "stdout", delay)));
        }
        if (redirect.contains("stderr")) {
            originalErr = System.err;
            System.setErr(new PrintStream(new LogEventOutputStream(logger, "stderr", delay)));
        }
    }
    protected void stopService() {
        if (originalOut != null)
            System.setOut(originalOut);
        if (originalErr != null)
            System.setErr(originalErr);
        logger.removeAllListeners ();
    }
    protected void destroyService() {
        // we don't destroy (that would unregister the logger from the
        // NameRegistrar) because other components might have references
        // to this logger.
        //
        // logger.destroy ();
    }
    private void addListener (Element e)
        throws ConfigurationException
    {
        QFactory factory = getServer().getFactory();
        if (QFactory.isEnabled(e)) {
            String clazz = e.getAttributeValue("class");
            LogListener listener = factory.newInstance(clazz);
            factory.setConfiguration(listener, e);
            logger.addListener(listener);
        }
    }
}
