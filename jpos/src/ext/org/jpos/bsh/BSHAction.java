/*
 * Copyright (c) 2004 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.bsh;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import bsh.Interpreter;

import org.jpos.ui.UI;
import org.jpos.ui.UIAware;
import org.jdom.Element;

public class BSHAction implements ActionListener, UIAware {
    public UI ui;

    public BSHAction () {
        super();
    }

    public void setUI (UI ui, Element e) {
        this.ui = ui;
    }

    public void actionPerformed (ActionEvent ev) {
        String bshSource = ev.getActionCommand();
        try {
            Interpreter bsh = new Interpreter ();
            bsh.source (bshSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

