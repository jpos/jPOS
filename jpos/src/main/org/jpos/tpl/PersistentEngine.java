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

package org.jpos.tpl;

import java.io.*;
import java.sql.*;
import org.jpos.iso.ISOUtil;
import org.jpos.util.Logger;
import org.jpos.util.LogSource;
import org.jpos.util.LogEvent;
import org.jpos.core.Configuration;
import org.jpos.core.Configurable;
import org.jpos.core.ConfigurationException;

/**
 * DataSource implementation used by PersistentPeer implementations
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 */
public class PersistentEngine implements LogSource, Configurable {
    Configuration cfg;
    Logger logger;
    String realm;

    /**
     * @param cfg Configuration
     * @param logger logger
     * @param realm logger's realm
     * @throws ConfigurationException
     */
    public PersistentEngine (Configuration cfg, Logger logger, String realm) 
	throws ConfigurationException
    {
        super();
	this.cfg = cfg;
	this.logger = logger;
	this.realm  = realm;
	initEngine ();
    }
    /**
     * no args constructor
     */
    public PersistentEngine () {
	super();
    }
    /**
     * Implements Configurable
     * @param cfg Configuration
     * @throws ConfigurationException
     */
    public void setConfiguration (Configuration cfg)
	throws ConfigurationException
    {
	this.cfg = cfg;
	initEngine();
    }
    private void initEngine () throws ConfigurationException {
	initJDBC();
    }
    private void initJDBC() throws ConfigurationException {
        try {
            Class.forName(cfg.get("jdbc.driver")).newInstance();
        } catch (Exception e) {
	    throw new ConfigurationException (e);
        }
    }
    public synchronized Connection getConnection() {
	for (;;) {
	    try {
		String url  = cfg.get ("jdbc.url");
		String user = cfg.get ("jdbc.user");
		String pass = cfg.get ("jdbc.password");
		return DriverManager.getConnection(url,user,pass);
	    } catch (SQLException e) {
		Logger.log (new LogEvent(this, "sql-connection", e));
		try {
		    Thread.sleep (2000);
		} catch (InterruptedException ex) { }
	    }
	}
    }
    public void releaseConnection (Connection conn) {
	// Connection pooling hook
	try {
	    conn.close();
	} catch (SQLException e) {
	    Logger.log (new LogEvent(this, "sql-release-connection", e));
	}
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
    /**
     * finds a peer for this object
     * @param obj Main object
     * @throws NoPeerException
     */
    public PersistentPeer getPeer (Object obj) throws NoPeerException {
	if (obj instanceof PersistentPeer) 
	    return (PersistentPeer) obj;
	else {
	    try {
		Class c = Class.forName (obj.getClass().getName()+"Peer");
		return (PersistentPeer) c.newInstance();
	    } catch (Exception e) {
		throw new NoPeerException (e.toString());
	    }
	}
    }
    /**
     * Execute SQL Update
     * @param sql sql command
     * @exception SQLException
     */
    public void executeUpdate (String sql) throws SQLException {
	Connection conn = getConnection();
	try {
	    executeUpdate (sql, conn);
	} finally {
	    releaseConnection (conn);
	}
    }
    /**
     * Execute SQL Update
     * @param sql  sql command
     * @param conn sql connection
     * @exception SQLException
     */
    public void executeUpdate (String sql, Connection conn) 
	throws SQLException
    {
	Statement s = null;
	try {
	    s = conn.createStatement();
	    if (logger != null && logger.hasListeners()) 
		Logger.log (new LogEvent (this, "sql-update", sql));
	    s.executeUpdate (sql);
	} finally {
	    if (s != null)
		s.close();
	}
    }
    public ResultSet executeQuery (String sql) throws SQLException {
	Connection conn = getConnection();
	try {
	    return executeQuery (sql, conn);
	} finally {
	    releaseConnection (conn);
	}
    }
    /**
     * Execute SQL Query. 
     * @param sql  sql command
     * @param conn sql connection
     * @return ResultSet (please close() it after using - thanks)
     * @exception SQLException
     */
    public ResultSet executeQuery (String sql, Connection conn) 
	throws SQLException
    {
	Statement s = null;
	ResultSet rs;
	try {
	    s = conn.createStatement();
	    if (logger != null && logger.hasListeners()) 
		Logger.log (new LogEvent (this, "sql-query", sql));
	    rs = s.executeQuery (sql);
	} finally {
	    if (s != null)
		s.close();
	}
        s.close();
        return rs;
    }

    /**
     * creates a new persistent object
     * @param o object to create
     * @throws NoPeerException
     * @throws SQLException
     */
    public void create (Object o) 
	throws NoPeerException, SQLException
    {
	PersistentPeer peer = getPeer(o);
	peer.setPersistentEngine (this);
	peer.create (o);
    }
    /**
     * load object from persistent storage
     * @param o object to load
     * @throws NoPeerException
     * @throws SQLException
     */
    public void load (Object o) 
	throws NoPeerException, SQLException, NotFoundException
    {
	PersistentPeer peer = getPeer(o);
	peer.setPersistentEngine (this);
	peer.load (o);
    }
    /**
     * remove object from persistent storage
     * @param o object to load
     * @throws NoPeerException
     * @throws SQLException
     */
    public void remove (Object o) 
	throws NoPeerException, SQLException, NotFoundException
    {
	PersistentPeer peer = getPeer(o);
	peer.setPersistentEngine (this);
	peer.remove (o);
    }
    /**
     * update object to persistent storage
     * @param o object to load
     * @throws NoPeerException
     * @throws SQLException
     */
    public void update (Object o) 
	throws NoPeerException, SQLException, NotFoundException
    {
	PersistentPeer peer = getPeer(o);
	peer.setPersistentEngine (this);
	peer.update(o);
    }

    public long getOID (Connection conn) throws SQLException {
	String sql = "SELECT last_insert_id()";
	ResultSet rs = executeQuery (sql, conn);
	long oid = rs.getLong (1);
	rs.close();
	return oid;
    }
}

