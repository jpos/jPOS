/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.jpos.core.XmlConfigurable;
import org.jpos.core.ConfigurationException;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogSource;

/** A TransactionParticipant which´s prepare, commit and abort methods can be 
 *  specified through beanshell scripts. <BR>
 *
 *  To indicate what code to execute for any of the methods just add an element 
 *  named 'prepare', 'commit' or 'abort' contained in that of the participant. <BR>
 *
 *  See BSHMethod for details on the syntax of these elemets. The value to return 
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
public class BSHTransactionParticipant extends SimpleLogSource implements TransactionParticipant, XmlConfigurable {
    
    protected BSHMethod prepareMethod;
    protected BSHMethod commitMethod;
    protected BSHMethod abortMethod;
    
    /** Creates a new instance of BSHTransactionParticipant */
    public BSHTransactionParticipant() {
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
        Logger.log(ev);
    }
    
    protected void defaultCommit(long id, Serializable context, LogEvent ev) {}
    
    public int prepare(long id, java.io.Serializable context) {
        LogEvent ev = new LogEvent(this, "prepare");
        int result = ABORTED | READONLY;
        if (prepareMethod != null) {
            try {
                result = ((Integer) executeMethod(prepareMethod, id, context, ev, "result")).intValue();
            } catch (Exception ex) {
                ev.addMessage(ex);
            }
        } else {
            result = defaultPrepare(id, context, ev);
        }
        ev.addMessage("result", Integer.toBinaryString(result));
        Logger.log(ev);
        return result;
    }
    
    protected int defaultPrepare(long id, Serializable context, LogEvent ev) {
        return PREPARED | READONLY;
    }
    
    public void setConfiguration(Element e) throws ConfigurationException {
	try {
            prepareMethod = BSHMethod.createBshMethod(e.getChild("prepare"));
            commitMethod = BSHMethod.createBshMethod(e.getChild("commit"));
            abortMethod = BSHMethod.createBshMethod(e.getChild("abort"));
        } catch (Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }
    
    protected Object executeMethod(BSHMethod m, long id, Serializable context, LogEvent evt, String resultName) 
    throws EvalError, FileNotFoundException, IOException {
        Map params = new HashMap();
        params.put("context", context);
        params.put("id", new Long(id));
        params.put("evt", evt);
        return m.execute(params, resultName);
    }
          
}
