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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alejandro Revilla
 *
 * creates a tabbed pane
 * i.e:
 * <pre>
 *  &lt;tabbed-pane font="xxx"&gt;
 *   &lt;pane title="xxx" icon="optional/icon/file.jpg"
 *            action="yyy" command="zzz"&gt;
 *   ...
 *   ...
 *   &lt;/pane&gt;
 *  &lt;/tabbed-pane&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
@SuppressWarnings("unchecked")
public class JTabbedPaneFactory implements UIFactory, ChangeListener {
    UI ui;
    List actions = new ArrayList();
    JTabbedPane p;

    public JComponent create (UI ui, Element e) {
        this.ui    = ui;
        p = new JTabbedPane ();

        String font = e.getAttributeValue ("font");
        if (font != null) 
            p.setFont (Font.decode (font));

        Iterator iter = e.getChildren ("pane").iterator();
        while (iter.hasNext()) {
            add (p, (Element) iter.next ());
        }
        p.addChangeListener(this);
        return p;
    }

    private void add (JTabbedPane p, Element e) {
        if (e != null) {
            Icon icon = null;
            String iconFile = e.getAttributeValue ("icon");
            if (iconFile != null)
                icon = new ImageIcon (iconFile);
            p.addTab (e.getAttributeValue ("title"), icon, ui.create (e));

            String action[] = new String[2];
            action[0]=e.getAttributeValue ("command");
            action[1]=e.getAttributeValue ("action");
            actions.add(action);
        }
    }

    public void stateChanged (ChangeEvent e) {
        try {
            String action[] = new String[2];
            action = (String[]) actions.get(p.getSelectedIndex());
            Object al = ui.get (action[1]);
            if (al instanceof ActionListener) {
                ActionEvent ae = new ActionEvent(this,0,action[0]);
                ((ActionListener) al).actionPerformed(ae);
            }
        } catch (Exception f) {
            f.printStackTrace();
        }
    }
}

