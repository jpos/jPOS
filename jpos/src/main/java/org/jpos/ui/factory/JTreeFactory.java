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

package org.jpos.ui.factory;

import org.jdom2.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Alejandro Revilla
 *
 * Creates a JTree
 * i.e:
 * <pre>
 *  &lt;tree&gt;
 *   &lt;node&gt;
 *    ...
 *   &lt;/node&gt;
 *   ...
 *   ...
 *   &lt;node&gt;
 *    ...
 *   &lt;/node&gt;
 *  &lt;/tree&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
@SuppressWarnings("unchecked")
public class JTreeFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        final UI parentUI = ui;
        final Map map = new HashMap ();
        final JTree tree = new JTree (getNode (e, map));
        String font = e.getAttributeValue ("font");
        if (font != null) 
            tree.setFont (Font.decode (font));
        tree.setRootVisible (e.getTextTrim().length() > 0);

        tree.addTreeSelectionListener (
            new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent evt) {
                    DefaultMutableTreeNode node = 
                        (DefaultMutableTreeNode) 
                            tree.getLastSelectedPathComponent();
                    if (node != null) {
                        String s = (String) map.get (node);
                        if (s != null) {
                            StringTokenizer st = new StringTokenizer (s);
                            String action  = st.nextToken ();
                            String command = null;
                            if (st.hasMoreTokens ())
                                command = st.nextToken ();

                            ActionListener al = 
                                (ActionListener) parentUI.get(action);
                            if (al != null) {
                                al.actionPerformed (
                                    new ActionEvent (node, 0, command)
                                );
                            }
                            // System.out.println ("ACTION: " + action);
                            // System.out.println ("COMMAND: " + command);
                        }
                    }
                }
            }
        );
        return tree;
    }

    private DefaultMutableTreeNode getNode (Element e, Map map) { 
        DefaultMutableTreeNode node = new DefaultMutableTreeNode (
                e.getTextTrim()
        );
        String action = e.getAttributeValue ("action");
        if (action != null) {
            String command = e.getAttributeValue ("command");
            if (command != null)
                action = action + " " + command;

            map.put (node, action);
        }
        Iterator iter = e.getChildren().iterator();
        while (iter.hasNext()) {
            node.add (getNode ((Element) iter.next (), map));
        }
        return node;
    }
}

