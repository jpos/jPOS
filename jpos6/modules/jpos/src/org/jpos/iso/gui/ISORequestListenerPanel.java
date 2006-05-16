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
 * ISORequestListenerPanel
 * Allows visualization of unhandled ISOMUX messages
 * @author apr@cs.com.uy
 * @see org.jpos.iso.ISOMsg
 * @see org.jpos.iso.ISOMUX
 * @see ISOMsgPanel
 */

/*
 * $Log$
 * Revision 1.9  2003/10/13 10:30:22  apr
 * tabs expanded to 8 spaces
 *
 * Revision 1.8  2003/05/16 04:15:18  alwyns
 * Import cleanups.
 *
 * Revision 1.7  2000/11/02 12:09:18  apr
 * Added license to every source file
 *
 * Revision 1.6  2000/04/14 18:27:42  apr
 * ISORequestListener may not be Observable
 *
 * Revision 1.5  2000/03/01 14:44:45  apr
 * Changed package name to org.jpos
 *
 * Revision 1.4  1999/09/06 17:20:22  apr
 * Added Logger SubSystem
 *
 * Revision 1.3  1999/08/06 11:40:12  apr
 * expand -4
 *
 * Revision 1.2  1999/07/29 15:55:12  apr
 * Added LOG_CAPACITY checks
 *
 * Revision 1.1  1999/05/18 12:02:59  apr
 * Added GUI package
 *
 */

package org.jpos.iso.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;

public class ISORequestListenerPanel extends JPanel implements Observer {

    private static final long serialVersionUID = -1786048717180010741L;
    /**
     * @serial
     */
    DefaultListModel log;
    /**
     * @serial
     */
    String symbolicName;
    public static final int LOG_CAPACITY = 250;

    public ISORequestListenerPanel (
        ISORequestListener requestListener,
        String symbolicName)
    {
        super();
        this.symbolicName = symbolicName;
        setLayout(new FlowLayout());
        setBorder(BorderFactory.createRaisedBevelBorder());
        log = new DefaultListModel();
        add(createPanel());
        if (requestListener instanceof Observable)
            ((Observable)requestListener).addObserver(this);
    }
    public final String getSymbolicName() {
        return symbolicName;
    }
    public final ListModel getLog() {
        return log;
    }

    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof ISOMsg) {
            ISOMsg m = (ISOMsg) arg;
            log.addElement(m);
            if (log.getSize() > LOG_CAPACITY) 
                log.remove(0);
        }
    }

    private JPanel createPanel() {
        JPanel A = new JPanel() {

            private static final long serialVersionUID = -6042644671679973897L;

            public Insets getInsets() {
                return new Insets(10,10,10,10);
            }
        };

        A.setLayout(new BorderLayout());

        JLabel l = new JLabel(symbolicName);
        A.add(l, BorderLayout.NORTH);

        final JList logList = new JList(log);
        logList.setPrototypeCellValue("9999 99999999 999999");

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ISOMsg m = (ISOMsg) logList.getSelectedValue();
                if (m != null) {
                    JFrame f = new JFrame(m.toString());
                    ISOMsgPanel p = new ISOMsgPanel(m);
                    f.getContentPane().add(p);
                    f.pack();
                    f.show();
                }
            }
        };
        logList.addMouseListener(mouseListener);

        JScrollPane scrollPane = new JScrollPane(logList);
        scrollPane.setPreferredSize(new Dimension(170,200));
        A.add(scrollPane, BorderLayout.SOUTH);
        return A;
    }
}
