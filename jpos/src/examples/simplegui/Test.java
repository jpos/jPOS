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
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.SimpleLogListener;
import org.jpos.util.SystemMonitor;

public class Test extends JPanel implements Runnable {
    static JFrame frame;
    ISOMUX mux;
    ISOChannel channel;

    public Test () {
        super();
	setLayout(new BorderLayout());
        add (createChannelPanel());
    }

    private ISOChannelPanel createChannelPanel() {
	Logger logger = new Logger();
	logger.addListener (new SimpleLogListener (System.out));

	channel = 
	    new ASCIIChannel("localhost", 8000, new ISO87APackager());
	mux = new ISOMUX (channel);
	((LogSource)channel).setLogger (logger, "channel");
	mux.setLogger (logger, "mux");
        Thread t = new Thread(mux);
	t.start();
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
		if (!channel.isConnected())
		    channel.connect();
		channel.send (m);
		// ISORequest req = new ISORequest (m);
		// mux.queue (req);
		// req.getResponse (10000);
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
