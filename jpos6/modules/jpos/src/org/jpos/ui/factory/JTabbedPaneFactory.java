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
package org.jpos.ui.factory;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;

/**
 * @author Alejandro Revilla
 *
 * creates a tabbed pane
 * i.e:
 * <pre>
 *  &lt;tabbed-pane font="xxx"&gt;
 *   &lt;pane title="xxx" icon="optional/icon/file.jpg"
 *            action="yyy" command="zzz"&gt;
 *   ...
 *   ...
 *   &lt;/pane&gt;
 *  &lt;/tabbed-pane&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class JTabbedPaneFactory implements UIFactory, ChangeListener {
    UI ui;
    List actions = new ArrayList();
    JTabbedPane p;

    public JComponent create (UI ui, Element e) {
        this.ui    = ui;
        p = new JTabbedPane ();

        String font = e.getAttributeValue ("font");
        if (font != null) 
            p.setFont (Font.decode (font));

        Iterator iter = e.getChildren ("pane").iterator();
        while (iter.hasNext()) {
            add (p, (Element) iter.next ());
        }
        p.addChangeListener(this);
        return p;
    }

    private void add (JTabbedPane p, Element e) {
        if (e != null) {
            Icon icon = null;
            String iconFile = e.getAttributeValue ("icon");
            if (iconFile != null)
                icon = new ImageIcon (iconFile);
            p.addTab (e.getAttributeValue ("title"), icon, ui.create (e));

            String action[] = new String[2];
            action[0]=e.getAttributeValue ("command");
            action[1]=e.getAttributeValue ("action");
            actions.add(action);
        }
    }

    public void stateChanged (ChangeEvent e) {
        try {
            String action[] = new String[2];
            action = (String[]) actions.get(p.getSelectedIndex());
            Object al = ui.get (action[1]);
            if (al instanceof ActionListener) {
                ActionEvent ae = new ActionEvent(this,0,action[0]);
                ((ActionListener) al).actionPerformed(ae);
            }
        } catch (Exception f) {
            f.printStackTrace();
        }
    }
}

