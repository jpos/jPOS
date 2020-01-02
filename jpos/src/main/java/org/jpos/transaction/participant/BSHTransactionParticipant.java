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

package org.jpos.transaction.participant;

import bsh.EvalError;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.QFactory;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogSource;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** A TransactionParticipant whose prepare, commit and abort methods can be 
 *  specified through beanshell scripts. <BR>
 *
 *  To indicate what code to execute for any of the methods just add an element 
 *  named 'prepare', 'commit' or 'abort' contained in that of the participant. <BR>
 *
 *  See BSHMethod for details on the syntax of these elements. The value to return 
 *  in the prepare method should be stored in the script variable named "result".
 *  None of these tags are mandatory. <BR>
 *
 *  You can subclass BSHTransactionParticipant and override the default... 
 *  methods. That way you can provide default behaviour for a participant and 
 *  override it at deploy time through scripts. 
 * 
 * @see BSHMethod
 * @author  AMarques
 */
@SuppressWarnings("unchecked")
public class BSHTransactionParticipant extends SimpleLogSource
    implements TransactionParticipant, AbortParticipant, XmlConfigurable 
{
    
    protected BSHMethod prepareMethod;
    protected BSHMethod prepareForAbortMethod;
    protected BSHMethod commitMethod;
    protected BSHMethod abortMethod;

    boolean trace;
    
    /** Creates a new instance of BSHTransactionParticipant */
    public BSHTransactionParticipant() {
        super();
    }
    
    public void abort(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "abort");
        if (abortMethod != null) {
            try {
                executeMethod(abortMethod, id, context, ev, "");
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } else {
            defaultAbort(id, context, ev);
        }
        if (trace)
            Logger.log(ev);
    }
    
    protected void defaultAbort(long id, Serializable context, LogEvent ev) {}
    
    public void commit(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "commit");
        if (commitMethod != null) {
            try {
                executeMethod(commitMethod, id, context, ev, "");
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } else {
            defaultCommit(id, context, ev);
        }
        if (trace)
            Logger.log(ev);
    }
    
    protected void defaultCommit(long id, Serializable context, LogEvent ev) {}
    
    public int prepare(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "prepare");
        int result = ABORTED | READONLY;
        if (prepareMethod != null) {
            try {
                result = (Integer) executeMethod(prepareMethod, id, context, ev, "result");
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } else {
            result = defaultPrepare(id, context, ev);
        }
        ev.addMessage("result", Integer.toBinaryString(result));
        if (trace)
            Logger.log(ev);
        return result;
    }

    public int prepareForAbort(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "prepare-for-abort");
        int result = ABORTED | READONLY;
        if (prepareForAbortMethod != null) {
            try {
                result = (Integer) executeMethod(prepareForAbortMethod, id, context, ev, "result");
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } 
        ev.addMessage("result", Integer.toBinaryString(result));
        if (trace)
            Logger.log(ev);
        return result;
    }
    
    protected int defaultPrepare(long id, Serializable context, LogEvent ev) {
        return PREPARED | READONLY;
    }
    
    public void setConfiguration(Element e) throws ConfigurationException {
	try {
            prepareMethod = BSHMethod.createBshMethod(e.getChild("prepare"));
            prepareForAbortMethod = BSHMethod.createBshMethod(e.getChild("prepare-for-abort"));
            commitMethod = BSHMethod.createBshMethod(e.getChild("commit"));
            abortMethod = BSHMethod.createBshMethod(e.getChild("abort"));
            trace = "yes".equals (QFactory.getAttributeValue (e, "trace"));
        } catch (Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }
    
    protected Object executeMethod(BSHMethod m, long id, Serializable context, LogEvent evt, String resultName) 
    throws EvalError, IOException {
        Map params = new HashMap();
        params.put("context", context);
        params.put("id", id);
        params.put("evt", evt);
        params.put("self", this);
        return m.execute(params, resultName);
    }
}

