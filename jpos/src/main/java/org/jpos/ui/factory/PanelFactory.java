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

package org.jpos.ui.factory;

import org.jdom2.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;

import javax.swing.*;

/**
 * @author Alejandro Revilla
 *
 * creates a panel (with an optional "id")
 *
 * Panel's id is registered in UI object registrar and can be
 * used for re-configuration
 * i.e:
 * <pre>
 *  &lt;panel id="xx"&gt;
 *   ...
 *   ...
 *  &lt;/panel&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 * @see org.jpos.ui.action.Redirect
 */
public class PanelFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        JPanel p = new JPanel ();
        if (e.getChildren().size() > 0)
            p.add (ui.create (e));
        return p;
    }
}

