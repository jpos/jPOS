/*
 * $Log$
 * Revision 1.9  2000/04/26 00:47:47  apr
 * Temporary workaround allows ISOChannelPanel to Observe ISOServer as well
 * as ISOChannel
 *
 * Revision 1.8  2000/04/24 01:55:38  apr
 * clone logged message
 *
 * Revision 1.7  2000/04/16 21:08:28  apr
 * ISOChannel is now an interface.
 * Channel implementation moved to org.jpos.iso.channel package
 *
 * Revision 1.6  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  1999/09/06 17:20:19  apr
 * Added Logger SubSystem
 *
 * Revision 1.4  1999/08/06 11:40:10  apr
 * expand -4
 *
 * Revision 1.3  1999/07/29 15:55:03  apr
 * Added LOG_CAPACITY checks
 *
 * Revision 1.2  1999/07/08 13:44:10  apr
 * Added acquirer support
 * Removed 'System.outs'
 *
 * Revision 1.1  1999/05/18 12:02:58  apr
 * Added GUI package
 *
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
import java.util.Observer;
import java.util.Observable;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jpos.iso.*;

public class ISOChannelPanel extends JPanel implements Observer {
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
                else if (m.isOutgoing())
                    meter.setValue(imti, mti);

                // log.insertElementAt(m,0);
                log.addElement((ISOMsg)m.clone());
                if (log.getSize() > LOG_CAPACITY) 
                    log.remove(0);

            } catch (ISOException e) { 
                meter.setValue(ISOMeter.mass, "ERROR");
            }
            meter.setValue(ISOMeter.mass);
        }
	if (o instanceof ISOChannel) {
	    ISOChannel c = (ISOChannel) o;
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

    private JPanel createCountersPanel() {
        JPanel A = new JPanel() {
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
    public ISOMeter getISOMeter() {
        return meter;
    }
}
