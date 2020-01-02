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

package org.jpos.rc;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CMFConverter implements IRCConverter, Configurable {
    private static final Map<Integer, RC> rcs = new HashMap<>();
    private static final Map<String, IRC> ircs = new HashMap<>();
    private Configuration cfg;

    static {
        try {
            load ("org/jpos/rc/CMF.properties");
            load ("META-INF/org/jpos/rc/CMF.properties");
        } catch (IOException ignored) { }
    }

    public CMFConverter() {
        super();
    }

    public CMFConverter(Configuration cfg) {
        this.cfg = cfg;
    }

    @Override
    public RC convert(IRC irc) {
        String s = cfg != null ? cfg.get(Long.toString(irc.irc()), null) : null;
        if (s != null) {
            return buildRC(s);
        } else if (rcs.containsKey(irc.irc()))
            return rcs.get(irc.irc());

        s = irc.irc() > 9999 ? Long.toString(irc.irc()) : ISOUtil.zeropad(irc.irc(),4);
        return new SimpleRC(s, (irc instanceof CMF ? ((CMF)irc).name().replace("_", " ") : null));
    }

    @Override
    public IRC convert (RC rc) {
        IRC irc = ircs.get(rc.rc());
        if (irc == null) {
            irc =  CMF.valueOf(Integer.parseInt(rc.rc()));
        }
        return irc;
    }

    private static void load (String base) throws IOException {
        try (InputStream in=loadResourceAsStream(base)) {
            if (in != null)
                addBundle(new PropertyResourceBundle(in));
        }
    }
    private static InputStream loadResourceAsStream(String name) {
        InputStream in = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null)
            in = contextClassLoader.getResourceAsStream(name);
        if (in == null)
            in = CMFConverter.class.getClassLoader().getResourceAsStream(name);
        return in;
    }

    private static SimpleRC buildRC (String s) {
        String[] ss = ISOUtil.commaDecode(s);
        String rc = null;
        String display = null;
        if (ss.length > 0)
            rc = ss[0];
        if (ss.length > 1)
            display = ss[1];
        Objects.requireNonNull(rc, "Invalid result code");
        return new SimpleRC (rc, display);
    }

    private static void addBundle(ResourceBundle r) {
        Enumeration en = r.getKeys();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String value = r.getString(key);
            RC rc = buildRC(value);
            int irc = Integer.parseInt(key);
            rcs.put (irc, rc);
            ircs.put (rc.rc(), CMF.valueOf(irc));
        }
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }
}
