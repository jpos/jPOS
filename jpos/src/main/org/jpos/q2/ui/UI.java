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

package org.jpos.q2.ui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.jpos.q2.QBean;
import org.jpos.q2.QBeanSupport;

public class UI extends QBeanSupport {
    JFrame mainFrame;

    public UI () {
        super();
    }

    public void startService () throws JDOMException {
        Element ui = getPersist ();
        if (!"ui".equals (ui.getName()))
            ui = ui.getChild ("ui");

        if (ui != null) {
            JFrame frame = initFrame (ui);
            frame.setJMenuBar (menuBar (ui.getChild ("menubar")));
            frame.show ();
        }
    }

    public void stopService () {
        if (mainFrame != null)
            mainFrame.dispose ();
    }

    private JFrame initFrame (Element ui) {
        Element caption = ui.getChild ("caption");
        mainFrame = caption == null ?  
            new JFrame () :
            new JFrame (caption.getText());

        JOptionPane.setRootFrame (mainFrame);
        mainFrame.getContentPane().setLayout(new BorderLayout());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(getDimension (ui, screenSize));

        //
        // mainFrame.setLocation(
        //    screenSize.width/2 - WIDTH/2,
        //    screenSize.height/2 - HEIGHT/2);
        // mainFrame.setSize(640, 480);
        // mainFrame.setCursor(
        //   Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
        // );
        //
        
        return mainFrame;
    }

    private JMenuBar menuBar (Element ui) {
        JMenuBar mb = new JMenuBar ();
        Iterator iter = ui.getChildren ("menu").iterator();
        while (iter.hasNext()) 
            mb.add (menu ((Element) iter.next()));

        return mb;
    }
    private JMenu menu (Element m) {
        JMenu menu = new JMenu (m.getAttributeValue ("id"));
        setItemAttributes (menu, m);
        Iterator iter = m.getChildren ().iterator();
        while (iter.hasNext()) 
            addMenuItem (menu, (Element) iter.next());
        return menu;
    }
    private void addMenuItem (JMenu menu, Element m) {
        String tag = m.getName ();

        if ("menuitem".equals (tag)) {
            JMenuItem item = new JMenuItem (m.getAttributeValue ("id"));
            setItemAttributes (item, m);
            menu.add (item);
        } else if ("menuseparator".equals (tag)) {
            menu.addSeparator ();
        } else if ("button-group".equals (tag)) {
            addButtonGroup (menu, m);
        } else if ("check-box".equals (tag)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem (
                m.getAttributeValue ("id")
            );
            setItemAttributes (item, m);
            item.setState (
                "true".equals (m.getAttributeValue ("state"))
           
            );
            menu.add (item);
        } else if ("menu".equals (tag)) {
            menu.add (menu (m));
        }
    }
    private void addButtonGroup (JMenu menu, Element m) {
        ButtonGroup group = new ButtonGroup();
        Iterator iter = m.getChildren ("radio-button").iterator();
        while (iter.hasNext()) {
            addRadioButton (menu, group, (Element) iter.next());
        }
    }
    private void addRadioButton (JMenu menu, ButtonGroup group, Element m) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem
            (m.getAttributeValue ("id"));
        setItemAttributes (item, m);
        item.setSelected (
            "true".equals (m.getAttributeValue ("selected"))
        );
        group.add (item);
        menu.add (item);
    }

    private Dimension getDimension (Element e, Dimension def) {
        String w = e.getAttributeValue ("width");
        String h = e.getAttributeValue ("height");

        return new Dimension (
           w != null ? Integer.parseInt (w) : def.width,
           h != null ? Integer.parseInt (h) : def.height
        );
    }
    private void setItemAttributes (AbstractButton b, Element e) 
    {
        String s = e.getAttributeValue ("accesskey");
        if (s != null && s.length() == 1)
            b.setMnemonic (s.charAt(0));

        String icon = e.getAttributeValue ("icon");
        if (icon != null) {
            try {
                b.setIcon (new ImageIcon (new URL (icon)));
            } catch (MalformedURLException ex) {
                ex.printStackTrace ();
            }
        }
    }
}

