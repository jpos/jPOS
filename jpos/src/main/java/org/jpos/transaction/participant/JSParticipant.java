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

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.QFactory;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.Serializable;

/**
 * A TransactionParticipant whose prepare, commit and abort methods can be
 * specified through JS scripts. <BR>
 * To indicate what code to execute for any of the methods just add an element
 *  named 'prepare', 'commit' or 'abort' contained in that of the participant. <BR>
 *
 *  The value to return
 *  in the prepare method should be stored in the script variable named "result".
 *  None of these tags are mandatory. <BR>
 *
 * Usage:
 *
 * <pre>
 *     Add a transaction participant like this:
 *     &lt;participant class="org.jpos.transaction.participant.JSParticipant" logger="Q2" realm="js"
 *     src='deploy/test.js' /&gt;
 *
 *     test.js may look like this (all functions are optional)
 *
 *     var K = Java.type("org.jpos.transaction.TransactionConstants");
 *     var prepare = function(id, ctx) {
 *       var map = ctx.getMap();
 *       ctx.log ("Prepare has been called");
 *       ctx.log (map.TIMESTAMP);
 *       map.NEWPROPERTY='ABC';
 *       return K.PREPARED;
 *     }
 *
 *     var prepareForAbort = function(id, ctx) {
 *       ctx.put ("Test", "Test from JS transaction $id");
 *       ctx.log ("prepareForAbort has been called");
 *       return K.PREPARED;
 *     }
 *     var commit = function(id, ctx) {
 *       ctx.log ("Commit has been called");
 *     }
 *
 *     var abort = function(id, ctx) {
 *       ctx.log ("Abort has been called");
 *     }
 * </pre>
 *
 * @author  @apr (based on AMarques' BSHTransactionParticipant)
 */
@SuppressWarnings("unchecked")
public class JSParticipant extends Log
    implements TransactionParticipant, AbortParticipant, XmlConfigurable 
{
    private Invocable js;
    boolean trace;
    boolean hasPrepare;
    boolean hasPrepareForAbort;
    boolean hasCommit;
    boolean hasAbort;

    public int prepare (long id, Serializable context) {
        return hasPrepare ? invokeWithResult("prepare", id, context) : PREPARED | READONLY;
    }
    public int prepareForAbort (long id, Serializable context) {
        return hasPrepareForAbort ? invokeWithResult("prepareForAbort", id, context) : PREPARED | READONLY;
    }
    public void commit(long id, Serializable context) {
        if (hasCommit)
            invokeNoResult("commit", id, context);
    }

    public void abort(long id, Serializable context) {
        if (hasAbort)
            invokeNoResult("abort", id, context);
    }

    public void setConfiguration(Element e) throws ConfigurationException {
	    try (FileReader src = new FileReader(QFactory.getAttributeValue(e, "src")))  {
            trace = "yes".equals(QFactory.getAttributeValue(e, "trace"));
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval(src);
            js = (Invocable) engine;
            hasPrepare = hasFunction ("prepare");
            hasPrepareForAbort = hasFunction ("prepareForAbort");
            hasCommit = hasFunction ("commit");
            hasAbort = hasFunction ("abort");
        } catch (Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }

    private boolean hasFunction (String functionName) throws ConfigurationException {
        try {
            js.invokeFunction(functionName, 0L, new Context());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ScriptException e) {
            throw new ConfigurationException (e);
        }
    }

    private int invokeWithResult (String functionName, long id, Serializable context) {
        try {
            return (Integer) js.invokeFunction(functionName, id, context);
        } catch (Exception e) {
            if (context instanceof Context) {
                Context ctx = (Context) context;
                ctx.log(e);
            } else {
                warn(id, e);
            }
            return ABORTED;
        }
    }
    private void invokeNoResult (String functionName, long id, Serializable context) {
        try {
            js.invokeFunction(functionName, id, context);
        } catch (Exception e) {
            if (context instanceof Context) {
                Context ctx = (Context) context;
                ctx.log(e);
            } else {
                warn(id, e);
            }
        }
    }
}
