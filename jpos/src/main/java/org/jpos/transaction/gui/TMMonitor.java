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

package org.jpos.transaction.gui;

import org.jpos.transaction.TransactionStatusListener;
import org.jpos.transaction.TransactionStatusEvent;
import org.jpos.transaction.TransactionManager;
import org.jpos.ui.UI;
import org.jpos.util.TPS;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TMMonitor extends JPanel
        implements TransactionStatusListener, SwingConstants, ActionListener, AncestorListener
{
    UI ui;
    TransactionManager txnmgr;
    JTable table;
    AbstractTableModel model;
    TransactionStatusEvent[] events;
    JLabel tps = new JLabel("0");
    JLabel tpsAvg = new JLabel("0.00");
    JLabel tpsPeak = new JLabel("0");
    JLabel inTransit = new JLabel("0");
    JLabel outstanding = new JLabel("0");
    Timer timer;

    static final Font SMALL  = Font.decode ("terminal-plain-8");
    static final Font LARGE  = Font.decode ("terminal-plain-18");

    Color[] color = new Color[] {
        /* READY               */ Color.white,
        /* PREPARING           */ new Color (0xb3, 0xb3, 0xff), // blue
        /* PREPARING_FOR_ABORT */ new Color (0xff, 0xa3, 0xa3), // red
        /* COMMITTING          */ new Color (0xd1, 0xff, 0xd1), // green
        /* ABORTING            */ new Color (0xff, 0xa3, 0xa3), // red
        /* DONE                */ Color.white,
        /* PAUSED              */ new Color (0xff, 0x7f, 0x50)  // orange
    };

    public TMMonitor (UI ui, TransactionManager tmmgr) {
        super();
        this.ui = ui;
        this.txnmgr = tmmgr;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        model = createModel ();

        table = createTable (model);
        JScrollPane scrollPane = new JScrollPane(table);

        add(createTPSPanel(), BorderLayout.EAST);
        add(scrollPane, BorderLayout.CENTER);
        tmmgr.addListener(this);
        addAncestorListener(this);
    }
    public void update(TransactionStatusEvent e) {
        if (ui.isDestroyed()) {
            return;
        }
        int row = e.getSession();
        events[row] = e;
        model.fireTableRowsUpdated(row, row);
        // table.getSelectionModel().setSelectionInterval(row, row);
        setBackgroundColor (row, color[e.getState().intValue()]);
        inTransit.setText (Long.toString (txnmgr.getInTransit()));
        outstanding.setText (Long.toString (txnmgr.getOutstandingTransactions()));
    }
    private void setBackgroundColor (int row, Color color) {
        for (int i=0; i<model.getColumnCount(); i++) {
            ((DefaultTableCellRenderer)table.getCellRenderer(row, i)).setBackground(color);
        }
    }
    private JTable createTable (TableModel model) {
        JTable table = new JTable (model);
        table.setSurrendersFocusOnKeystroke(true);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(true);
        table.setCellSelectionEnabled(false);
        table.setDoubleBuffered(true);
        TableColumnModel tcm = table.getColumnModel();

        tcm.getColumn(0).setPreferredWidth(10);
        tcm.getColumn(1).setPreferredWidth(25);
        tcm.getColumn(2).setPreferredWidth(100);
        tcm.getColumn(3).setPreferredWidth(600);
        return table;
    }
    private JComponent createTPSPanel () {
        JPanel outer = new JPanel (new BorderLayout());

        JPanel p = new JPanel (new GridLayout (5,2));

        add (p, tps, "TPS");
        add (p, tpsPeak, "Peak TPS");
        add (p, tpsAvg, "Avg TPS");
        add (p, inTransit, "InTransit");
        add (p, outstanding, "Queue");

        JPanel blackFiller = new JPanel();
        outer.add (p, BorderLayout.NORTH);
        blackFiller.setBackground(Color.black);
        outer.add (blackFiller, BorderLayout.CENTER);
        outer.setPreferredSize(new Dimension (170, 0));
        return outer;
    }

    private void add (JPanel p, JLabel l, String description) {
        setLabelStyle (l, LARGE, RIGHT);
        p.add (l);
        p.add (setLabelStyle (new JLabel (description), SMALL, LEFT));
    }
    private JLabel setLabelStyle (JLabel l, Font f, int alignment) {
        l.setBorder (BorderFactory.createLoweredBevelBorder ());
        l.setFont (f);
        l.setOpaque (true);
        l.setForeground(Color.green);
        l.setBackground(Color.black);
        // l.setAlignment(alignment);
        l.setHorizontalAlignment(alignment);
        l.setVerticalAlignment(BOTTOM);
        return l;
    }
    private AbstractTableModel createModel () {
        events = new TransactionStatusEvent[txnmgr.getActiveSessions()];
        return new AbstractTableModel() {
            String[] columnName = new String[] {
               "#", "id", "State", "Info"
            };
            Class[] columnClass = new Class[] {
                Integer.class, Long.class, String.class, String.class
            };
            public int getColumnCount() {
                return 4;
            }
            public int getRowCount() {
                return txnmgr.getActiveSessions();
            }
            @Override
            public String getColumnName(int columnIndex) {
                return columnName[columnIndex];
            }
            public Class getColumnClass(int columnIndex) {
                return columnClass[columnIndex];
            }
            public Object getValueAt(int row, int col) {
                if (events[row] != null) {
                    switch (col) {
                        case 0:
                            return row;
                        case 1:
                            return events[row].getId();
                        case 2:
                            return events[row].getStateAsString();
                        case 3:
                            return events[row].getInfo();
                    }
                }
                return "";
            }
        };
    }

    public void actionPerformed(ActionEvent e) {
        TPS t = txnmgr.getTPS();
        tps.setText (Integer.toString (t.intValue()));
        tpsAvg.setText (String.format ("%.2f", t.getAvg()));
        tpsPeak.setText (Integer.toString (t.getPeak()));
    }

    public void ancestorAdded(AncestorEvent event) {
        if (timer == null) {
            timer = new Timer (1000, this);
            timer.start();
        }
    }

    public void ancestorRemoved(AncestorEvent event) {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public void ancestorMoved(AncestorEvent event) { }
}
