/* $Id: */

package simplegui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import org.jpos.iso.*;
import org.jpos.iso.gui.*;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.jpos.core.Sequencer;
import org.jpos.core.VolatileSequencer;

public class Test extends JPanel implements Runnable {
    static JFrame frame;
    ISOMUX mux;

    public Test () {
        super();
	setLayout(new BorderLayout());
        add (createChannelPanel());
    }

    private ISOChannelPanel createChannelPanel() {
	ISOChannel channel = 
	    new ASCIIChannel("localhost", 8000, new ISO87APackager());
	mux = new ISOMUX (channel);
        new Thread(mux).start();
	return new ISOChannelPanel (channel, "localhost:8000");
    }

    private JMenuBar createMenuBar() {
        Action action;
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenuItem mi;
        JMenu file = new JMenu("File");
        file.setMnemonic('f');

        mi = (JMenuItem) file.add (new JMenuItem("About"));
        mi.setMnemonic('a');
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame.getContentPane(),
                    "jPOS GUI Client example", 
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        file.addSeparator();
        mi = new JMenuItem("Quit");
        mi.setMnemonic('q');
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(mi);
        return file;
    }
    public void run () {
	Sequencer seq = new VolatileSequencer();
	for (;;) {
	    try { 
		Thread.sleep (10000);
		Date d = new Date();
		ISOMsg m = new ISOMsg();
		m.setMTI ("0800");
		m.set (new ISOField (11,
		    ISOUtil.zeropad(
			new Integer(seq.get ("traceno")).toString(),6)
		    )
		);
		m.set (new ISOField(12,ISODate.getTime(d)));
		m.set (new ISOField(13,ISODate.getDate(d))); 
		m.set (new ISOField(41, "00000001"));
		m.set (new ISOField(70, "301"));
		ISORequest req = new ISORequest (m);
		mux.queue (req);
		req.getResponse (10000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public static void main (String args[]) {
        frame = new JFrame("jPOS GUI Client example");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        frame.addWindowListener(l);

	Test test = new Test();
        JOptionPane.setRootFrame(frame);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setJMenuBar(test.createMenuBar());
        frame.getContentPane().add(test, BorderLayout.CENTER);
	frame.pack();
        frame.show();
	new Thread (test).start();
    }
}
