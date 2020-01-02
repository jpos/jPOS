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
import org.jpos.util.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Alejandro Revilla
 *
 * a log listener component
 *
 * i.e:
 * <pre>
 *  &lt;log-listener scrollable="true"
 *      logger="Q2" font="fixed-normal-12"
 *      max-events="100" max-lines="50" /&gt;
 * </pre>
 *
 * @see org.jpos.ui.UIFactory
 */
public class LogListenerFactory implements UIFactory {
    public JComponent create (UI ui, Element e) {
        JTextArea textArea = new JTextArea (25, 80);
        String font = e.getAttributeValue ("font");
        if (font != null) 
            textArea.setFont (Font.decode (font));
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.white);
        String logId = e.getAttributeValue ("logger", "Q2");
        try {
            int maxEvents = Integer.parseInt (
                e.getAttributeValue ("max-events", "100")
            );
            int maxLines = Integer.parseInt (
                e.getAttributeValue ("max-lines", "1000")
            );
            LogProducer logger = (LogProducer) NameRegistrar.get ("logger." + logId);
            logger.addListener (
                new Listener (logger, ui, textArea, maxEvents, maxLines)
            );
        } catch (NameRegistrar.NotFoundException ex) {
            textArea.setText (ex.toString ());
        }
        return textArea;
    }
    public static class Listener implements LogListener, Runnable {
        final LogProducer logger;
        JTextArea text;
        UI ui;
        int cnt;
        int maxEvents;
        int maxLines;
        public Listener 
            (LogProducer logger, UI ui, JTextArea text, int maxEvents, int maxLines)
        {
            super ();
            this.ui = ui;
            this.text = text;
            this.logger = logger;
            this.cnt = 0;
            this.maxEvents = maxEvents;
            this.maxLines  = maxLines;
            text.setEditable(false);
        }
        public void run () {
            if (ui.isDestroyed ()) {
                logger.removeListener (this);
                text.setText ("");
                return;
            }
            int lc = text.getLineCount ();
            if (lc > maxLines) {
                try {
                    int startOffset = text.getLineStartOffset (maxLines);
                    int endOffset = text.getLineEndOffset(lc-1);
                    text.getDocument ().remove (startOffset, endOffset-startOffset);
                } catch (BadLocationException ex) {
                    text.setText (ex.toString());
                }
            }
        }
        public LogEvent log (LogEvent evt) {
            if (ui.isDestroyed ()) {
                SwingUtilities.invokeLater (this);
                return evt;
            }
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream (str);
            evt.dump (ps, "");
            text.insert (str.toString(), 0);

            if (++cnt % maxEvents == 0) {
                SwingUtilities.invokeLater (this);
            }
            return evt;
        }
    }
}

