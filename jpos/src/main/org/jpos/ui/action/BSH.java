package org.jpos.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import bsh.Interpreter;

public class BSH implements ActionListener {

    public BSH() {
        super();
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
