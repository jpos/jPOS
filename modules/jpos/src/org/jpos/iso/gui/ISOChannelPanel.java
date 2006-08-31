/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

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
                    ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.TX]), 6)
                );
                meter.setNegativeCounter(
                    ISOUtil.zeropad(Integer.toString(cnt[ISOChannel.RX]), 6)
                );
            } catch (ISOException e) { }
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
        meter.start();
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
        for (int i=0; i<protectFields.length; i++) {
            int f = protectFields[i];
            if (m.hasField (f))
                m.set (f, ISOUtil.protect (m.getString(f)));
        }
    }
    private void checkHidden (ISOMsg m) throws ISOException {
        for (int i=0; i<wipeFields.length; i++) {
            int f = wipeFields[i];
            if (m.hasField (f))
                m.set (f, "[WIPED]");
        }
    }
}

