package org.jpos.q2.example;

import java.net.URL;

import java.awt.Font;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.BorderFactory;

import org.jpos.q2.QBeanSupport;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public class Splash extends QBeanSupport implements Runnable {
    SplashWindow window;

    public Splash () {
        super();
    }
    public void run () {
        try {
            URL url = new URL ("http://www.jpos.org/q2/q2.jpg");
            ImageIcon icon = new ImageIcon (url);
            window = new SplashWindow (icon);
            window.setVisible(true);
            window.toFront();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startService () {
        new Thread (this).start ();
    }

    public void stopService() {
        if (window != null) 
            window.dispose();
        window = null;
    }

    class SplashWindow extends JWindow implements MouseListener {
        private JLabel text;

        public SplashWindow (ImageIcon img) {
            init (img);
        }

        protected void init (ImageIcon img) {
            JPanel panel = (JPanel) getContentPane();
            JLabel imgLabel;
            if (img == null) {
                imgLabel = new JLabel();
            } else {
                imgLabel = new JLabel (img);
            }
            imgLabel.setBorder (
                BorderFactory.createLineBorder(Color.black, 1)
            );
            text = new JLabel("jPOS.org", JLabel.CENTER);
            text.setFont (new Font("Sans-Serif", Font.BOLD, 14));
            text.setBorder (BorderFactory.createEtchedBorder());

            JPanel panSouth = new JPanel();
            panSouth.setLayout(new BorderLayout());

            panSouth.add(text, BorderLayout.NORTH);

            panel.setLayout (new BorderLayout());
            panel.add (imgLabel, BorderLayout.CENTER);
            panel.add (panSouth, BorderLayout.SOUTH);

            panel.setBorder (
                BorderFactory.createBevelBorder (
                    javax.swing.border.BevelBorder.RAISED
                )
            );
            pack ();
            Dimension size = getSize ();
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (scr.width - size.width) / 2;
            int y = (scr.height - size.height) / 2;
            setBounds (x, y, size.width, size.height);
            addMouseListener (this);
        }
        public void mouseClicked  (MouseEvent e) { 
            stop ();
        }
        public void mouseEntered  (MouseEvent e) { }
        public void mouseExited   (MouseEvent e) { }
        public void mousePressed  (MouseEvent e) { }
        public void mouseReleased (MouseEvent e) { }

        public void setText(String txt) {
            text.setText(txt);
        }

    }
}

