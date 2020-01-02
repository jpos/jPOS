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
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.gui.ISOChannelPanel;
import org.jpos.iso.gui.ISOMeter;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;
import org.jpos.util.NameRegistrar;

import javax.swing.*;
import java.util.Observable;

/**
 * @author Alejandro Revilla
 *
 * Creates an ISOMeter component
 * i.e:
 * <pre>
 *  &lt;iso-meter idref="id" scroll="true|false" refresh="nnn"/&gt
 * </pre>
 * @see org.jpos.ui.UIFactory
 */

public class ISOMeterFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        ISOChannelPanel icp = null;
        try {
            Object obj = NameRegistrar.get (e.getAttributeValue ("idref"));

            if (obj instanceof ISOChannel) {
                icp = new ISOChannelPanel ((ISOChannel) obj, e.getText ());
            } else if (obj instanceof Observable) {
                icp = new ISOChannelPanel (e.getText());
                ((Observable)obj).addObserver (icp);
            }
            ISOMeter meter = icp.getISOMeter ();
            if ("false".equals (e.getAttributeValue ("scroll")))
                meter.setScroll (false);

            String protect = e.getAttributeValue ("protect");
            if (protect != null)
                icp.setProtectFields (ISOUtil.toIntArray (protect));
            String wipe = e.getAttributeValue ("wipe");
            if (wipe != null)
                icp.setWipeFields (ISOUtil.toIntArray (wipe));

            String refresh = e.getAttributeValue ("refresh");
            if (refresh != null)
                meter.setRefresh (Integer.parseInt (refresh));
        } catch (Exception ex) {
            ex.printStackTrace ();
            return new JLabel (ex.getMessage());
        }
        return icp;
    }
}

