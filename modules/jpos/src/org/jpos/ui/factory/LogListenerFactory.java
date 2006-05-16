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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

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
        String logId = e.getAttributeValue ("logger", "Q2");
        try {
            int maxEvents = Integer.parseInt (
                e.getAttributeValue ("max-events", "100")
            );
            int maxLines = Integer.parseInt (
                e.getAttributeValue ("max-lines", "1000")
            );
            Logger logger = (Logger) NameRegistrar.get ("logger." + logId);
            logger.addListener (
                new Listener (logger, ui, textArea, maxEvents, maxLines)
            );
        } catch (NameRegistrar.NotFoundException ex) {
            textArea.setText (ex.toString ());
        }
        return textArea;
    }
    public class Listener implements LogListener, Runnable {
        final Logger logger;
        JTextArea text;
        UI ui;
        int cnt;
        int maxEvents;
        int maxLines;
        public Listener 
            (Logger logger, UI ui, JTextArea text, int maxEvents, int maxLines)
        {
            super ();
            this.ui = ui;
            this.text = text;
            this.logger = logger;
            this.cnt = 0;
            this.maxEvents = maxEvents;
            this.maxLines  = maxLines;
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
                    int offset = text.getLineStartOffset (lc - maxLines);
                    text.getDocument ().remove (0, offset);
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
            text.append (str.toString());

            if ((++cnt % maxEvents) == 0) {
                SwingUtilities.invokeLater (this);
            }
            return evt;
        }
    }
}

