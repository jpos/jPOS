/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.transaction.participant;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.GroupSelector;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.Serializable;

/**
 * BeanShell-backed {@link GroupSelector} that evaluates a {@code <select>} script
 * to choose the next participant group, falling back to {@link #defaultSelect}
 * when the script returns {@code null}.
 *
 * @author  AMarques
 */
public class BSHGroupSelector extends BSHTransactionParticipant implements GroupSelector {
    /** Default constructor; no instance state to initialise. */
    public BSHGroupSelector() {}

    /** Compiled BSH method invoked to determine the active group. */
    protected BSHMethod selectMethod;
    
    public void setConfiguration(Element e) throws ConfigurationException {
        super.setConfiguration(e);
        try {
            selectMethod = BSHMethod.createBshMethod(e.getChild("select"));
        } catch (Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
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
    
    /**
     * Hook returning the default group when the BSH script returns {@code null}.
     * Subclasses may override to provide deterministic fallback selection.
     *
     * @param id transaction id
     * @param context transaction context
     * @return default group identifier (empty string by default)
     */
    public String defaultSelect(long id, Serializable context) {
        return "";
    }
        
}
