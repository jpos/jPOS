/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
 * creates a vertical split pane
 * i.e:
 * <pre>
 *  &lt;vsplit&gt;
 *   &lt;top&gt;...&lt;/top&gt;
 *   &lt;bottom&gt;...&lt;/bottom&gt;
 *  &lt;/vsplit&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class VSplitFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            ui.create (e.getChild ("top")),
            ui.create (e.getChild ("bottom"))
        );
        String dividerAttr = e.getAttributeValue ("divider");
        if (dividerAttr != null) {
            splitPane.setDividerLocation (
                Integer.parseInt (dividerAttr)
            );
        }
        return splitPane;
    }
}

