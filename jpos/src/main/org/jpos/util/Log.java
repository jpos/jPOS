/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project 
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may 
 *    appear in the software itself, if and wherever such third-party 
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse 
 *    or promote products derived from this software without prior 
 *    written permission. For written permission, please contact 
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS 
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package org.jpos.util;

/**
 * Represents a LogSource and adds several helpers
 *
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 * @see LogSource
 */
public class Log implements LogSource {
    protected Logger logger;
    protected String realm;

    public static final String TRACE   = "trace";
    public static final String DEBUG   = "debug";
    public static final String INFO    = "info";
    public static final String WARN    = "warn";
    public static final String ERROR   = "error";
    public static final String FATAL   = "fatal";

    public Log () {
        super();
    }
    public static Log getLog (String logName, String realm) {
        return new Log (Logger.getLogger (logName), realm);
    }
    public Log (Logger logger, String realm) {
        setLogger (logger, realm);
    }
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }
    public void setLogger (Logger logger) {
        this.logger = logger;
    }
    public void setRealm (String realm) {
        this.realm = realm;
    }
    public void trace (Object detail) {
        Logger.log (createTrace (detail));
    }
    public void trace (Object detail, Object obj) {
        LogEvent evt = createTrace (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void debug (Object detail) {
        Logger.log (createDebug (detail));
    }
    public void debug (Object detail, Object obj) {
        LogEvent evt = createDebug (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void info (Object detail) {
        Logger.log (createInfo (detail));
    }
    public void info (Object detail, Object obj) {
        LogEvent evt = createInfo (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void warn (Object detail) {
        Logger.log (createWarn (detail));
    }
    public void warn (Object detail, Object obj) {
        LogEvent evt = createWarn (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void error (Object detail) {
        Logger.log (createError (detail));
    }
    public void error (Object detail, Object obj) {
        LogEvent evt = createError (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public void fatal (Object detail) {
        Logger.log (createFatal (detail));
    }
    public void fatal (Object detail, Object obj) {
        LogEvent evt = createFatal (detail);
        evt.addMessage (obj);
        Logger.log (evt);
    }
    public LogEvent createLogEvent (String level) {
        return new LogEvent (this, level);
    }
    public LogEvent createLogEvent (String level, Object detail) {
        return new LogEvent (this, level, detail);
    }
    public LogEvent createTrace () {
        return createLogEvent (TRACE);
    }
    public LogEvent createTrace (Object detail) {
        return createLogEvent (TRACE, detail);
    }
    public LogEvent createDebug() {
        return createLogEvent (TRACE);
    }
    public LogEvent createDebug(Object detail) {
        return createLogEvent (TRACE, detail);
    }
    public LogEvent createInfo () {
        return createLogEvent (INFO);
    }
    public LogEvent createInfo (Object detail) {
        return createLogEvent (INFO, detail);
    }
    public LogEvent createWarn () {
        return createLogEvent (WARN);
    }
    public LogEvent createWarn (Object detail) {
        return createLogEvent (WARN, detail);
    }
    public LogEvent createError () {
        return createLogEvent (ERROR);
    }
    public LogEvent createError (Object detail) {
        return createLogEvent (ERROR, detail);
    }
    public LogEvent createFatal () {
        return createLogEvent (FATAL);
    }
    public LogEvent createFatal (Object detail) {
        return createLogEvent (FATAL, detail);
    }
}

