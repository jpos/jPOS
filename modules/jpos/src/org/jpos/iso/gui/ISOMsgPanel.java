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
 * Revision 1.10  2006/03/29 20:32:22  marklsalter
 * Ask the message to be displayed for the highest field, rather than assume 128.
 *
 * Revision 1.9  2003/05/16 04:15:19  alwyns
 * Import cleanups.
 *
 * Revision 1.8  2002/12/16 12:47:25  apr
 * Minor changes to reduce new Doclet warnings.
 *
 * Revision 1.7  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.6  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.5  1999/09/06 17:20:21  apr
 * Added Logger SubSystem
 *
 * Revision 1.4  1999/08/06 13:52:08  apr
 * Added getValueAdjusting() check to avoid inner ISOMsgs showing twice
 *
 * Revision 1.3  1999/08/06 11:40:12  apr
 * expand -4
 *
 * Revision 1.2  1999/05/18 14:50:10  apr
 * Show ISOMsg fields in new frame
 *
 * Revision 1.1  1999/05/18 12:02:59  apr
 * Added GUI package
 *
 */

package org.jpos.iso.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;

/**
 * Called from ISOChannelPanel when you click on it's ISOMeter.
 * It enable field and header visualization by means of visual
 * components such as JTable
 *
 * @see ISOChannelPanel
 * @see ISORequestListenerPanel
 */
public class ISOMsgPanel extends JPanel {

    private static final long serialVersionUID = 7779880613544725704L;
    /**
     * @serial
     */
    ISOMsg m;
    /**
     * @serial
     */
    Vector validFields;
    public ISOMsgPanel(ISOMsg m, boolean withDump) {
        super();
        this.m = m;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        setValidFields();
        add(createISOMsgTable(), BorderLayout.CENTER);
        if (withDump)
            add(createISOMsgDumpPanel(), BorderLayout.SOUTH);
    }
    public ISOMsgPanel(ISOMsg m) {
        this(m, false);
    }
    private void setValidFields() {
        validFields = new Vector();
        for (int i=0; i<=m.getMaxField(); i++)
            if (m.hasField(i))
                validFields.addElement(new Integer(i));
    }
    private JComponent createISOMsgTable() {
        TableModel dataModel = new AbstractTableModel() {

            private static final long serialVersionUID = 8917029825751856951L;
            public int getColumnCount() {
                return 3;
            }
            public int getRowCount() {
                return validFields.size();
            }
            public String getColumnName(int columnIndex) {
                switch (columnIndex) {
                    case 0 :
                        return "#";
                    case 1 :
                        return "Content";
                    case 2 :
                        return "Description";
                    default:
                        return "";
                }
            }
            public Object getValueAt(int row, int col) {
                switch (col) {
                    case 0 :
                        return ((Integer)validFields.elementAt(row));
                    case 1 :
                        try {
                            int index =
                            ((Integer)validFields.elementAt(row)).intValue();

                            Object obj = m.getValue(index);
                            if (obj instanceof String) 
                                return obj.toString();
                            else if (obj instanceof byte[])
                                return ISOUtil.hexString((byte[]) obj);
                            else if (obj instanceof ISOMsg)
                                return "<ISOMsg>";
                        } catch (ISOException e) {
                            e.printStackTrace();
                        }   
                        break;
                    case 2 :
                        int i=((Integer)validFields.elementAt(row)).intValue();
                        ISOPackager p = m.getPackager();
                        return p.getFieldDescription(m,i);
                }
                return "<???>";
            }
        };
        JTable table = new JTable(dataModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(10);
        table.setPreferredScrollableViewportSize(
            new Dimension (500,table.getRowCount()*table.getRowHeight()));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;

                ListSelectionModel lsm =
                    (ListSelectionModel)e.getSource();
                if (!lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();
                    try {
                        int index = ((Integer)
                            validFields.elementAt(selectedRow)).intValue();

                        Object obj = m.getValue(index);
                        if (obj instanceof ISOMsg) {
                            ISOMsg sm = (ISOMsg) obj;
                            JFrame f = new JFrame("ISOMsg field "+index);
                            ISOMsgPanel p = new ISOMsgPanel(sm, false);
                            f.getContentPane().add(p);
                            f.pack();
                            f.show();
                        }
                    } catch (ISOException ex) {
                        ex.printStackTrace();
                    }   
                }
            }
        });

        JScrollPane scrollpane = new JScrollPane(table);
        return scrollpane;
    }
    JComponent createISOMsgDumpPanel() {
        JPanel p = new JPanel();
        JTextArea t = new JTextArea(3,20);

        p.setLayout(new BorderLayout());
        p.setBackground(Color.white);
        p.setBorder(BorderFactory.createLoweredBevelBorder());
        p.add(new JLabel("Dump", SwingConstants.LEFT),
            BorderLayout.NORTH);
        t.setFont(new Font ("Helvetica", Font.PLAIN, 8));
        t.setLineWrap(true);
        try {
            StringBuffer buf = new StringBuffer();
            if (m.getHeader() != null) 
                buf.append("--[Header]--\n" 
                    +ISOUtil.hexString(m.getHeader()) + "\n--[Msg]--\n");
            byte[] b = m.pack();
            buf.append (ISOUtil.hexString(b));
            t.setText(buf.toString());
        } catch (ISOException e) {
            t.setText(e.toString());
            t.setForeground(Color.red);
        }
        p.add(t, BorderLayout.CENTER);
        return p;
    }
}
