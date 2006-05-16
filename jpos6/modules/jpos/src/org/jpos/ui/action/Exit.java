package org.jpos.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIAware;

public class Exit implements ActionListener, UIAware {
    public UI ui;
    public int exitCode = 0;

    public Exit () {
        super();
    }
    public void setUI (UI ui, Element e) {
        this.ui = ui;
    }
    public void actionPerformed (ActionEvent ev) {
        ui.dispose ();
        try {
            exitCode = Integer.parseInt(ev.getActionCommand());
        } catch (Exception e) { }
        new Thread() {
            public void run() {
                System.exit (exitCode);
            }
        }.start();
    }
}

