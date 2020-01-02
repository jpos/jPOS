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

package org.jpos.transaction.gui;

import org.jpos.ui.UIFactory;
import org.jpos.ui.UI;
import org.jpos.transaction.TransactionManager;
import org.jpos.util.NameRegistrar;
import org.jdom2.Element;

import javax.swing.*;

public class TMMonitorFactory implements UIFactory {
    public JComponent create(UI ui, Element e) {
        try {
            TransactionManager tm =
                (TransactionManager) NameRegistrar.get (e.getAttributeValue ("transaction-manager"));
            return new TMMonitor (ui, tm);
        } catch (NameRegistrar.NotFoundException ex) {
            return new JTextArea (ex.toString());
        }
    }
}
