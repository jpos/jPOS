/**
 * ISOMsgPanel
 * Swing based GUI to ISOMsg
 * @author apr@cs.com.uy
 * @see uy.com.cs.jpos.iso.ISOMsg
 */

/*
 * $Log$
 * Revision 1.1  1999/05/18 12:02:59  apr
 * Added GUI package
 *
 */

package uy.com.cs.jpos.iso.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import uy.com.cs.jpos.iso.*;

public class ISOMeter extends JComponent implements ActionListener {
	Color color = new Color (255, 255, 255);
	Image im;
	Graphics img;
	Font fontBig, fontSmall;
	String positiveText, negativeText;
	Timer ti;
	/**
	 * handle ISOMeter's counters outside of this class in order
	 * to reduce 'int' to 'String' conversions.
	 */
	String positiveCounter, negativeCounter;
	int lastPositive, lastNegative;
	boolean connected;
	ISOChannelPanel parent;

	final static int width    = 200;
	final static int height   = 60;
	final static int mass     = height/2;

	int[] yPoints, xPoints;
	
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
		ti = new Timer(50, this);
	}

	public void showLogList() {
		JFrame f = new JFrame(parent.getSymbolicName());
		f.getContentPane().add(createLogList());
		f.validate();
		f.pack();
		f.setSize(200,250);
		f.show();
	}

	public void start() {
		ti.start();
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
		img.drawString (p, width-37, 13);
		img.drawString (n, width-37, height-3);
	}
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	public void update (Graphics g) {
		paint (g);
	}
}
