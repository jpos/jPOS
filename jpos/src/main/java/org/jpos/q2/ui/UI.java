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

package org.jpos.q2.ui;

import org.jdom2.Element;
import org.jpos.q2.QBeanSupport;
import org.jpos.ui.UIObjectFactory;
import org.jpos.util.NameRegistrar;

/**
 * @author Alejandro Revilla
 *
 * org.jpos.ui.UI adapter
 */
public class UI extends QBeanSupport implements UIObjectFactory {
    org.jpos.ui.UI ui;
    public UI () {
        super();
    }
    public void startService () throws Exception {
        Element config = getPersist ();
        String provider = 
            config.getAttributeValue ("provider", "org.jpos.ui.UI");
        ui = (org.jpos.ui.UI) getFactory().newInstance (provider);
        ui.setConfig (config);
        ui.setLog (getLog ());
        ui.setObjectFactory (this);
        ui.configure ();
        NameRegistrar.register (getName(), ui);
    }

    public void stopService () {
        NameRegistrar.unregister (getName());
        if (ui != null) 
            ui.dispose ();
    }
    public Object newInstance (String clazz) throws Exception {
        return getFactory().newInstance (clazz);
    }
} 

