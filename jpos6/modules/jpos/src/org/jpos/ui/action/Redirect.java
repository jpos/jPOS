package org.jpos.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIAware;

public class Redirect implements ActionListener, UIAware {
    public UI ui;
    public Redirect () {
        super();
    }
    public void setUI (UI ui, Element e) {
        this.ui = ui;
    }
    public void actionPerformed (ActionEvent ev) {
        StringTokenizer st = new StringTokenizer (ev.getActionCommand ());
        ui.reconfigure (
            st.nextToken(), 
            st.hasMoreTokens () ?  st.nextToken () : null
        );
    }
}

