/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jdom.Element;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;

import java.util.Iterator;

public class LoggerAdaptor extends QBeanSupport {
    Logger logger;

    protected void initService () {
        logger = Logger.getLogger (getName());
    }
    protected void startService () throws ConfigurationException {
        logger.removeAllListeners ();
        Iterator iter = getPersist ().getChildren ("log-listener").iterator();
        while (iter.hasNext()) 
            addListener ((Element) iter.next ());
    }
    protected void stopService() {
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
        String clazz  = e.getAttributeValue ("class");
        LogListener listener = (LogListener) factory.newInstance (clazz);
        if (listener instanceof Configurable) {
            try {
                ((Configurable) listener).setConfiguration (
                    factory.getConfiguration (e)
                );
            } catch (ConfigurationException ex) {
                throw new ConfigurationException (ex);
            }
        }
        
        logger.addListener (listener);
    }
}

