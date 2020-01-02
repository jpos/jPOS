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

package org.jpos.bsh;

import bsh.Interpreter;
import org.jdom2.Element;
import org.jpos.ui.UI;

import javax.swing.*;

/**
 * @author Alejandro Revilla
 *
 * UI with BSH script support
 */
public class BSHUI extends UI {
    protected JComponent doScript (JComponent component, Element e) {
        try {
            Interpreter bsh = new Interpreter ();
            bsh.set ("component", component);
            bsh.set ("config", e);
            bsh.set ("log", getLog());
            bsh.set ("ui", this);
            bsh.eval ("import java.awt.*;");
            bsh.eval ("import java.awt.event.*;");
            bsh.eval ("import javax.swing.*;");
            bsh.eval ("import org.jpos.ui.*;");
            bsh.eval (e.getText ());
            return (JComponent) bsh.get ("component");
        } catch (Exception ex) {
            warn (ex);
            return new JLabel (ex.toString ());
        }
    }
}

