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

package org.jpos.ui.action;

import org.jdom2.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIAware;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Exit implements ActionListener, UIAware {
    public UI ui;
    public int exitCode = 0;

    public Exit () {
        super();
    }
    public void setUI (UI ui, Element e) {
        this.ui = ui;
    }
    public void actionPerformed (ActionEvent ev) {
        ui.dispose ();
        try {
            exitCode = Integer.parseInt(ev.getActionCommand());
        } catch (Exception ignored) { /* NOPMD*/ }
        new Thread() {
            public void run() {
                System.exit (exitCode);
            }
        }.start();
    }
}

