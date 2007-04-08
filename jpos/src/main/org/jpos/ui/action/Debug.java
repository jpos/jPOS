package org.jpos.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Debug implements ActionListener {
    public Debug () {
        super();
    }
    public void actionPerformed (ActionEvent ev) {
        System.out.println ("Action command: "+ev.getActionCommand ());
        System.out.println (ev.toString ());
        System.out.println ("");
    }
}
