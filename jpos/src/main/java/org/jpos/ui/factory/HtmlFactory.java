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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * @author Alejandro Revilla
 *
 * Creates an html browser/editor
 * i.e:
 * <pre>
 *  &lt;html editable="false" follow-links="true" scrollable="true"&gt;
 *    http://jpos.org
 *  &lt;/html&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class HtmlFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        try {
            JEditorPane editorPane = new JEditorPane (e.getText());
            editorPane.setEditable (
                "true".equals (e.getAttributeValue ("editable"))
            );
            if ("true".equals (e.getAttributeValue ("follow-links")))
                editorPane.addHyperlinkListener (new Listener ());
            return editorPane;
        } catch (Exception ex) {
            return new JLabel (ex.getMessage());
        }
    }
    static class Listener implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                    HTMLDocument doc = (HTMLDocument)pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    try {
                        pane.setPage(e.getURL());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}

