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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * ISOMsgPanel
 * Swing based GUI to ISOMsg
 * @author apr@cs.com.uy
 * @author Kris Leite <kleite at imcsoftware.com>
 * @see org.jpos.iso.ISOMsg
 */
@SuppressWarnings("deprecation")
public class ISOMeter extends JComponent implements Runnable {

    private static final long serialVersionUID = -1770533267122111538L;
    /**
     * @serial
     */
    Color color = new Color (255, 255, 255);
    /**
     * @serial
     */
    Image im;
    /**
     * @serial
     */
    Graphics img;
    /**
     * @serial
     */
    Font fontBig, fontSmall;
    /**
     * @serial
     */
    String positiveText;
    /**
     * @serial
     */
    String negativeText;
    /**
     * @serial
     */
    Timer ti;
    /**
     * handle ISOMeter's counters outside of this class in order
     * to reduce 'int' to 'String' conversions.
     * @serial
     */
    String positiveCounter;
    /**
     * @serial
     */
    String negativeCounter;
    /**
     * @serial
     */
    int lastPositive;
    /**
     * @serial
     */
    int lastNegative;
    /**
     * @serial
     */
    boolean connected;
    /**
     * @serial
     */
    ISOChannelPanel parent;

    final static int width    = 200;
    final static int height   = 60;
    final static int mass     = height/2;
    final static int MAX_VALUE = 1000;

    /**
     * @serial
     */
    int[] yPoints;
    /**
     * @serial
     */
    int[] xPoints;
    /**
     * counter to keep the scrolling active
     */
    int continueScroll;
    /**
     * used to determine if to scroll mark to end of graph
     */
    boolean scroll = true;
    /**
     * Refresh panel in millseconds
     */
    int refreshPanel = 50;

    private Image imb;
    private Thread repaintThread;

    public ISOMeter(ISOChannelPanel parent) {
        super();
        this.parent = parent;

        fontBig   = new Font ("Helvetica", Font.ITALIC, mass*3/4);
        fontSmall = new Font ("Helvetica", Font.PLAIN, 10);
        yPoints = new int[width];
        xPoints = new int[width];
        for (int i=0; i<width; i++) {
            xPoints[i] = i;
            yPoints[i] = mass;
        }
        positiveText  = null;
        negativeText  = null;
        positiveCounter = negativeCounter = "";
        connected     = false;

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                showLogList();
            }
        };
        addMouseListener(mouseListener);
    }

    public synchronized void start() {
        if (repaintThread == null) {
            repaintThread = new Thread (this,"ISOMeter");
            repaintThread.setPriority (Thread.NORM_PRIORITY-1);
            repaintThread.start();
        }
    }

    public void showLogList() {
        JFrame f = new JFrame(parent.getSymbolicName());
        f.getContentPane().add(createLogList());
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.validate();
        f.pack();
        f.setSize(width,width+50);
        f.show();
    }

    public JComponent createLogList() {
        final JList logList = new JList(parent.getLog());
        JPanel A = new JPanel();
        A.setLayout(new BorderLayout());

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ISOMsg m = (ISOMsg) logList.getSelectedValue();
                if (m != null) {
                    JFrame f = new JFrame(
                        parent.getSymbolicName()+":"+m.toString());
                    ISOMsgPanel p = new ISOMsgPanel(m);
                    f.getContentPane().add(p);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.pack();
                    f.show();
                }
            }
        };
        logList.addMouseListener(mouseListener);

        logList.setPrototypeCellValue("9999 99999999 999999");
        JScrollPane scrollPane = new JScrollPane(logList);
        A.add(scrollPane, BorderLayout.CENTER);
        return A;
    }

    public void setValue(int val) {
        int y = mass - val%1000 * height / 2000;
        yPoints[width-1] = y;
        continueScroll = width;
        scroll();
    }

    public void setScroll (boolean scroll) {
        this.scroll = scroll;
    }
    public void setRefresh (int refreshPanel) {
        if (refreshPanel > 0)
            this.refreshPanel = refreshPanel;
    }
    public void setConnected(boolean connected) {
        if (this.connected != connected) {
            if (!scroll)
                if (connected)
                    continueScroll = width;
                else
                    continueScroll = 1;
            repaint();
        }
        this.connected = connected;
    }
    public void setPositiveCounter(String s) {
        positiveCounter = s;
    }
    public void setNegativeCounter(String s){
        negativeCounter = s;
    }
    public void setValue(int val, String textString) {
        setValue(val);
        if (val < 0) {
            negativeText = textString;
            lastNegative = 0;
        }
        else {
            positiveText = textString;
            lastPositive = 0;
        }
    }
    public void paint (Graphics g) {
        if (repaintThread == null)
            start();
        plot();
        g.drawImage (im, 0, 0, null);
    }
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    private void scroll() {
        System.arraycopy(yPoints, 1, yPoints, 0, width - 1);
        if (continueScroll > 0)
            continueScroll--;
    }
    public void plot() {
       if (im == null) {
            im = createImage(width, height);
            img = im.getGraphics ();
            img.setColor (Color.black);
            img.fillRoundRect (0, 0, width, height, 10, 10);
            img.clipRect (0, 0, width, height);
            plotGrid();

            /* save a copy of the image */
            imb = createImage(width, height);
            Graphics imbCopy = imb.getGraphics();
            imbCopy.drawImage (im, 0, 0, this);
        }
        img.drawImage (imb, 0, 0, this);
        if (continueScroll > 0)
            scroll();
        plotText(positiveText, lastPositive++, 3, mass-3);
        plotText(negativeText, lastNegative++, 3, height-3);
        plotCounters(positiveCounter, negativeCounter);
        img.setColor (connected ? Color.green : Color.red);
        img.drawPolyline(xPoints, yPoints, width);
    }
    private void plotGrid() {
        img.setColor(Color.blue);
        for (int i=0; i<width; i++)
            if (i % 20 == 0) 
                img.drawLine(i,0,i,height);
        for (int i=-1000; i<1000; i+= 200) {
            int y = mass + i*height/2000;
            img.drawLine(0,y,width,y);
        }
    }
    private void plotText(String t, int l, int x, int y) {
        if (t != null && l < 20) {
            img.setColor(Color.lightGray);
            img.setFont(fontBig);
            img.drawString (t, x, y);
        }
    }
    private void plotCounters(String p, String n) {
        img.setColor(Color.lightGray);
        img.setFont(fontSmall);
        img.drawString (p, width-55, 13);
        img.drawString (n, width-55, height-3);
    }
    public void run () {
        while (isShowing()) {
            if (continueScroll > 0)
                repaint();
            try { 
                Thread.sleep(refreshPanel);
            } catch (InterruptedException e) {
                // OK to ignore
            }
        }
        repaintThread = null;
    }
    public void update (Graphics g) {
        paint (g);
    }
}
