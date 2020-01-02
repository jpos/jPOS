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

/**
 * allows for easy visualization of channel utilization. 
 * It shows messages coming through in an
 * 'Oscilloscope' style clickeable window.
 * @see ISOMeter
 * @see ISOMsgPanel
 * @serial
 */

import org.jpos.iso.*;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ISOChannelPanel extends JPanel implements Observer {
    private static final long serialVersionUID = -8069489863639386589L;
    /**
     * @serial
     */
    ISOMeter meter;
    /**
     * @serial
     */
    DefaultListModel log;
    /**
     * @serial
     */
    String symbolicName;

    private int[] protectFields = null;
    private int[] wipeFields    = null;

    public static final int LOG_CAPACITY = 250;

    public ISOChannelPanel (ISOChannel channel, String symbolicName)
    {
        super();
        this.symbolicName = symbolicName;
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        log = new DefaultListModel();
        add(createCountersPanel());
        meter.setConnected(channel.isConnected());
        if (channel instanceof Observable) 
            ((Observable)channel).addObserver(this);
    }
    /**
     * Unconnected constructor allows for instantiation of
     * ISOChannelPanel without associated ISOChannel
     * (that can be attached later)
     */
    public ISOChannelPanel (String symbolicName) {
        super();
        this.symbolicName = symbolicName;
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        log = new DefaultListModel();
        add(createCountersPanel());
        meter.setConnected(false);
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
            try {
                String mti = (String) m.getValue(0);
                int imti   = Integer.parseInt(mti);
                if (m.isIncoming())
                    meter.setValue(-imti, mti);
                else 
                    meter.setValue(imti, mti);

                // log.insertElementAt(m,0);
                log.addElement(getProtectedClone (m));
                if (log.getSize() > LOG_CAPACITY) 
                    log.remove(0);

            } catch (ISOException e) { 
                meter.setValue(ISOMeter.mass, "ERROR");
            }
            meter.setValue(ISOMeter.mass);
        }
        if (o instanceof BaseChannel) {
            BaseChannel c = (BaseChannel) o;
            meter.setConnected(c.isConnected());
            int cnt[] = c.getCounters();
            try {
                meter.setPositiveCounter(
                    ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.TX % 1000000000]), 9)
                );
                meter.setNegativeCounter(
                    ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.RX] % 1000000000), 9)
                );
            } catch (ISOException e) { }
        } else if (o instanceof ISOServer) {
            final ISOServer server = (ISOServer) o;
            final Runnable updateIt = new Runnable() {
                public void run() {
                    ISOUtil.sleep (250L);
                    int active = server.getActiveConnections();
                    meter.setConnected(active > 0);
                    try {
                        meter.setPositiveCounter(
                            ISOUtil.zeropad(Integer.toString(active), 6)
                        );
                    } catch (ISOException e) { }
                    meter.repaint();
                }
            };
            SwingUtilities.invokeLater (updateIt);
        } else 
            meter.setConnected(true);
    }
    public ISOMeter getISOMeter() {
        return meter;
    }
    public void setProtectFields (int[] fields) {
        protectFields = fields;
    }
    public void setWipeFields (int[] fields) {
        wipeFields    = fields;
    }
    private JPanel createCountersPanel() {
        JPanel A = new JPanel() {

            private static final long serialVersionUID = 1175437215105556679L;

            public Insets getInsets() {
                return new Insets(10,10,10,10);
            }
        };

        A.setLayout(new BorderLayout());

        meter = new ISOMeter(this);

        JLabel l = new JLabel(symbolicName);
        A.add(l, BorderLayout.NORTH);
        A.add(meter, BorderLayout.CENTER);
        // meter.start(); -- ISOMeter has auto-start now
        return A;
    }
    private ISOMsg getProtectedClone (ISOMsg m) throws ISOException {
        ISOMsg pm = (ISOMsg) m.clone ();
        if (protectFields != null)
            checkProtected(pm);
        if (wipeFields != null)
            checkHidden (pm);
        return pm;
    }
    private void checkProtected (ISOMsg m) throws ISOException {
        for (int f : protectFields) {
            if (m.hasField(f))
                m.set(f, ISOUtil.protect(m.getString(f)));
        }
    }
    private void checkHidden (ISOMsg m) throws ISOException {
        for (int f : wipeFields) {
            if (m.hasField(f))
                m.set(f, "[WIPED]");
        }
    }
}

