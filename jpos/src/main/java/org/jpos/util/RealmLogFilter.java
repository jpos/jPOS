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

package org.jpos.util;


import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jdom2.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filters LogEvents by their realm
 *
 * RealmLogFilter is a filter for Event logs,
 * it should be defined _before_ other standard LogListeners
 * such as SimpleLogListener or RotateLogListeners.
 *
 * * i.e.
 *  <pre>
 *  <logger name="Q2">
 *  <log-listener class="org.jpos.util.SimpleLogListener"/>
 *  <log-listener class="org.jpos.util.RealmLogFilter">
 *    <property name="dump-interval" value="60000"/>
 *    <enabled>
 *      Q2.system
 *      my-realm
 *    </enabled>
 *  </log-listener>
 *  <log-listener class="org.jpos.util.RotateLogListener">
 *    <property name="file" value="/log/q2.log" />
 *    <property name="window" value="86400" />
 *    <property name="copies" value="5" />
 *    <property name="maxsize" value="1000000" />
 *    </log-listener>
 *  </logger>
 *  </pre>
 *
 * Order is important. In the previous example SimpleLogListener
 * will show all LogEvents before RealmLogFilter filters these
 * according to the list of enabled realms.
 *
 * Reads values configured inside <enabled></enabled> or
 * <disabled></disabled> to choose elements of which realm to log.
 * If <enabled></enabled> is defined, these realms ONLY will be logged. Disabled will not be taken into account.
 * If the <disabled></disabled> realms are defined, only this will not be shown, the rest will.
 * If none of them is defined the events will be logged.
 *
 * Those realms that had events but were filtered will be saved. These are logged at a certain interval defined
 * by the property dump-interval in an <ignored-realms> tag. Once logged these filtered realms are reset.
 *
 * @author Santiago Revilla
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 * @see org.jpos.core.XmlConfigurable
 */


public class RealmLogFilter implements LogListener, XmlConfigurable, Configurable {
    private Set<String> enabledRealms;
    private Set<String> disabledRealms;
    private Set<String> realmsMissed = new HashSet<>();
    private long lastDump = System.currentTimeMillis();
    private long dumpInterval;

    @Override
    public LogEvent log(LogEvent ev) {
        String realm = ev.getRealm();
        realm = realm != null ? realm.split("/")[0] : null;
        if (realmEnabled(realm)) {
            return ev;
        } else {
            realmsMissed.add(realm);
            if (dumpInterval > 0 && System.currentTimeMillis() - lastDump > dumpInterval) {
                LogEvent evt = new LogEvent("ignored-realms");
                evt.addMessage(realmsMissed);
                realmsMissed = new HashSet<>();
                lastDump = System.currentTimeMillis();
                return evt;
            }
            return null;
        }
    }

    @Override
    public void setConfiguration (Element e) throws ConfigurationException {
        Element enabled = e.getChild("enabled");
        Element disabled = e.getChild("disabled");
        if (enabled != null && !"".equals(enabled.getTextNormalize()))
            enabledRealms = new HashSet(Arrays.asList(enabled.getTextNormalize().split(" ")));
        if (disabled != null && !"".equals(disabled.getTextNormalize()))
            disabledRealms = new HashSet(Arrays.asList(disabled.getTextNormalize().split(" ")));;
    }
    
    private boolean realmEnabled (String realm) {
        if (enabledRealms != null && enabledRealms.size() > 0)
            return enabledRealms.contains(realm);
        if (disabledRealms != null && disabledRealms.size() > 0)
            return !disabledRealms.contains(realm);
        return true;
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        dumpInterval = cfg.getLong("dump-interval", 0);
    }
}