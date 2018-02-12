/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Alejandro Revilla
 *
 * Creates a JButton
 * i.e:
 * <pre>
 *  &lt;button id="xx" action="yyy" command="zzz"&gt;MyButton&lt;/button&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class JButtonFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        JButton button = new JButton (e.getText());
        button.setHorizontalAlignment(JLabel.CENTER);
        button.setBorder(new EmptyBorder(3, 3, 3, 3));
        String font = e.getAttributeValue ("font");
        if (font != null) 
            button.setFont (Font.decode (font));
        return button;
    }
}

