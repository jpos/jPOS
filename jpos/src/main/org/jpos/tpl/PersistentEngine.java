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
    Connection sqlConnection;
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
	sqlConnection = getConnection();
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
		Logger.log (new LogEvent(this, "sqlconnection", e));
		try {
		    Thread.sleep (2000);
		} catch (InterruptedException ex) { }
	    }
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
	Statement s = null;
	try {
	    s = sqlConnection.createStatement();
	    if (logger.hasListeners()) 
		Logger.log (new LogEvent (this, "sql-update", sql));
	    s.executeUpdate (sql);
	    s.close();
	} finally {
	    if (s != null)
		s.close();
	}
    }
    /**
     * Execute SQL Query. 
     * @param sql sql command
     * @return ResultSet (please close() it after using - thanks)
     * @exception SQLException
     */
    public ResultSet executeQuery (String sql) throws SQLException {
	Statement s = null;
	ResultSet rs;
	try {
	    s = sqlConnection.createStatement();
	    if (logger.hasListeners()) 
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
	throws NoPeerException, SQLException
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
	throws NoPeerException, SQLException
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
	throws NoPeerException, SQLException
    {
	PersistentPeer peer = getPeer(o);
	peer.setPersistentEngine (this);
	peer.update(o);
    }
}

