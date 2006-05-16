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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;

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

