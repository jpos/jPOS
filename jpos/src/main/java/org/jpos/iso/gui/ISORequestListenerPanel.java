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

package org.jpos.iso.gui;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("deprecation")
public class ISORequestListenerPanel extends JPanel implements Observer {

    private static final long serialVersionUID = -1786048717180010741L;
    /**
     * @serial
     */
    DefaultListModel log;
    /**
     * @serial
     */
    String symbolicName;
    public static final int LOG_CAPACITY = 250;

    public ISORequestListenerPanel (
        ISORequestListener requestListener,
        String symbolicName)
    {
        super();
        this.symbolicName = symbolicName;
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        log = new DefaultListModel();
        add(createPanel());
        if (requestListener instanceof Observable)
            ((Observable)requestListener).addObserver(this);
    }
    public final String getSymbolicName() {
        return symbolicName;
    }
    public final ListModel getLog() {
        return log;
    }

    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) arg;
            log.addElement(m);
            if (log.getSize() > LOG_CAPACITY) 
                log.remove(0);
        }
    }

    private JPanel createPanel() {
        JPanel A = new JPanel() {

            private static final long serialVersionUID = -6042644671679973897L;

            public Insets getInsets() {
                return new Insets(10,10,10,10);
            }
        };

        A.setLayout(new BorderLayout());

        JLabel l = new JLabel(symbolicName);
        A.add(l, BorderLayout.NORTH);

        final JList logList = new JList(log);
        logList.setPrototypeCellValue("9999 99999999 999999");

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ISOMsg m = (ISOMsg) logList.getSelectedValue();
                if (m != null) {
                    JFrame f = new JFrame(m.toString());
                    ISOMsgPanel p = new ISOMsgPanel(m);
                    f.getContentPane().add(p);
                    f.pack();
                    f.show();
                }
            }
        };
        logList.addMouseListener(mouseListener);

        JScrollPane scrollPane = new JScrollPane(logList);
        scrollPane.setPreferredSize(new Dimension(170,200));
        A.add(scrollPane, BorderLayout.SOUTH);
        return A;
    }
}
