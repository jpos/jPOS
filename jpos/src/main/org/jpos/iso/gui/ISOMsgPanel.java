/**
 * ISOMsgPanel
 * Swing based GUI to ISOMsg
 * @author apr@cs.com.uy
 * @see uy.com.cs.jpos.iso.ISOMsg
 */

/*
 * $Log$
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

package uy.com.cs.jpos.iso.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;

import uy.com.cs.jpos.iso.*;

/**
 * Called from ISOChannelPanel when you click on it's ISOMeter.<br>
 * It enable field and header visualization by means of visual
 * components such as JTable
 *
 * @see ISOChannelPanel
 * @see ISORequestListenerPanel
 */

public class ISOMsgPanel extends JPanel {
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
        this(m, true);
    }
    private void setValidFields() {
        validFields = new Vector();
        for (int i=0; i<=128; i++)
            if (m.hasField(i))
                validFields.addElement(new Integer(i));
    }
    private JComponent createISOMsgTable() {
        TableModel dataModel = new AbstractTableModel() {
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
