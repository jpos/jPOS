package org.jpos.apps.qsp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * QSP's GUI peer
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */

public class ControlPanel extends JPanel {
    static JFrame frame;
    QSP qsp;
    public ControlPanel (QSP qsp, int rows, int cols) {
	super();
	this.qsp = qsp;
	setLayout (new GridLayout(rows, cols));
    }
    public void showUp () {
        frame = new JFrame("jPOS QSP");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        frame.addWindowListener(l);
        JOptionPane.setRootFrame(frame);
        frame.getContentPane().setLayout(new BorderLayout());
        // frame.setJMenuBar(createMenuBar());
        frame.getContentPane().add(this, BorderLayout.CENTER);
	frame.show();
	frame.pack();
    }
}
