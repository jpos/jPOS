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

import bsh.BshClassManager;
import bsh.Interpreter;
import bsh.UtilEvalError;
import org.jdom2.Element;
import org.jpos.q2.QBeanSupport;

public class BSH extends QBeanSupport implements Runnable {
    protected Interpreter bsh;
    public void initService() {
        bsh = new Interpreter ();
        BshClassManager bcm = bsh.getClassManager();
        try {
            bcm.setClassPath(getServer().getLoader().getURLs());
        } catch (UtilEvalError e) {
            e.printStackTrace();
        }
        bcm.setClassLoader(getServer().getLoader());
    }
    public void startService() {
        new Thread (this, "BSH-" + getName()).start ();
    }
    public void run () {
        Element config = getPersist();
        try {
            bsh.set  ("qbean", this);
            bsh.set  ("log", getLog());
            bsh.set  ("cfg", getConfiguration());
            bsh.eval (config.getText());
            String source = config.getAttributeValue ("source");
            if (source != null)
                bsh.source (source);
        } catch (Throwable e) {
            getLog().warn (e);
        }
    }
}

