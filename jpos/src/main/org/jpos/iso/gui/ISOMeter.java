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

/**
 * ISOMsgPanel
 * Swing based GUI to ISOMsg
 * @author apr@cs.com.uy
 * @see org.jpos.iso.ISOMsg
 */

/*
 * $Log$
 * Revision 1.8  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.7  2000/04/22 02:39:49  apr
 * Back to 20 updates per second
 *
 * Revision 1.6  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  1999/09/19 21:39:29  apr
 * Changed from Timer based to Thread based timing (again)
 *
 * Revision 1.4  1999/09/06 17:20:20  apr
 * Added Logger SubSystem
 *
 * Revision 1.3  1999/08/06 11:40:11  apr
 * expand -4
 *
 * Revision 1.2  1999/07/27 12:14:16  apr
 * slow down ISOMeter (avoid hogging CPU on slow machines)
 *
 * Revision 1.1  1999/05/18 12:02:59  apr
 * Added GUI package
 *
 */

package org.jpos.iso.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jpos.iso.*;

public class ISOMeter extends JComponent implements Runnable {
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

    /**
     * @serial
     */
    int[] yPoints;
    /**
     * @serial
     */
    int[] xPoints;
    
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

    public void start() {
	Thread t = new Thread (this);
	t.setPriority (Thread.NORM_PRIORITY-1);
	t.setName ("ISOMeter");
	t.start();
    }

    public void showLogList() {
        JFrame f = new JFrame(parent.getSymbolicName());
        f.getContentPane().add(createLogList());
        f.validate();
        f.pack();
        f.setSize(200,250);
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
        int y = mass - (val*height/2000);
        yPoints[width-1] = y;
        scroll();
    }

    public void setConnected(boolean connected) {
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
        plot();
        g.drawImage (im, 0, 0, null);
    }
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    private void scroll() {
        for (int i=0; i<width-1; i++) 
            yPoints[i] = yPoints[i+1];
    }
    public void plot() {
        if (im == null) {
            im = createImage(width, height);
            img = im.getGraphics ();
        }
        img.setColor (Color.black);
        img.fillRoundRect (0, 0, width, height, 10, 10);
        img.clipRect (0, 0, width, height);
        scroll();
        plotGrid();
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
            int y = mass + (i*height/2000);
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
        img.drawString (p, width-45, 13);
        img.drawString (n, width-45, height-3);
    }
    public void run () {
	for (;;) {
	    repaint();
	    try { 
		Thread.sleep(50);
	    } catch (InterruptedException e) { }
	}
    }
    public void update (Graphics g) {
        paint (g);
    }
}
