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
import java.awt.*;

/**
 * @author Alejandro Revilla
 *
 * creates a panel with a Border layout
 * i.e:
 * <pre>
 *  &lt;border-layout&gt;
 *   &lt;north&gt;...&lt;/north&gt;
 *   &lt;south&gt;...&lt;/south&gt;
 *   &lt;east&gt;...&lt;/east&gt;
 *   &lt;west&gt;...&lt;/west&gt;
 *   &lt;center&gt;...&lt;/center&gt;
 *  &lt;/border-layout&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class BorderLayoutFactory implements UIFactory {
    UI ui;

    public JComponent create (UI ui, Element e) {
        this.ui    = ui;
        JPanel p = new JPanel (new BorderLayout ());

        add (p, e.getChild ("north"),  BorderLayout.NORTH);
        add (p, e.getChild ("south"),  BorderLayout.SOUTH);
        add (p, e.getChild ("east"),   BorderLayout.EAST);
        add (p, e.getChild ("west"),   BorderLayout.WEST);
        add (p, e.getChild ("center"), BorderLayout.CENTER);

        return p;
    }

    private void add (JPanel p, Element e, String location) {
        if (e != null)
            p.add (ui.create (e), location);
    }
}

