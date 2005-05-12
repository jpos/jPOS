/*
 * Copyright (c) 2005 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

import java.io.Serializable;
import org.jpos.q2.Q2ConfigurationException;
import org.jpos.transaction.GroupSelector;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * @author  AMarques
 */
public class BSHGroupSelector extends BSHTransactionParticipant implements GroupSelector {
    
    protected BSHMethod selectMethod;
    
    public void setConfiguration(org.jdom.Element e) throws Q2ConfigurationException {
        super.setConfiguration(e);
        try {
            selectMethod = BSHMethod.createBshMethod(e.getChild("select"));
        } catch (Exception ex) {
            throw new Q2ConfigurationException(ex.getMessage(), ex);
        }
    }    
    
    public String select(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "select");
        String result = null;
        if (selectMethod != null) {
            try {
                result = (String) executeMethod(selectMethod, id, context, ev, "result");
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } 
        if (result == null) {
            result = defaultSelect(id, context);
        }
        ev.addMessage("result", result);
        Logger.log(ev);
        return result;
    }
    
    public String defaultSelect(long id, Serializable context) {
        return "";
    }
        
}
