/*
 * $Log$
 * Revision 1.1  2000/01/23 16:12:11  apr
 * CVS devel sync
 *
 */

package uy.com.cs.jpos.core;

import java.io.*;
import java.sql.*;
import java.util.*;
import uy.com.cs.jpos.util.*;

/**
 * @author apr@cs.com.uy
 * @version $Id$
 * @since jPOS 1.1
 */
public class SQLBatchManager extends SimpleLogProducer implements BatchManager {
    Configuration cfg;
    Connection sqlConnection;

    public SQLBatchManager (Configuration cfg, Logger logger, String realm) {
	super();
	this.cfg = cfg;
	setLogger (logger, realm);
	initJDBC ();
	sqlConnection = getConnection();
    }
    private void initJDBC() {
        try {
            Class.forName(cfg.get("jdbc.driver")).newInstance();
        } catch (Exception e) {
            e.printStackTrace ();
            System.exit (0);
        }
    }
    private synchronized Connection getConnection() {
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
    private void fetchFinancial (FinancialTransaction t)
	throws SQLException, InvalidCardException 
    {
	Statement s = sqlConnection.createStatement();
	String query = "SELECT pan, exp FROM journal WHERE rrn = '" 
			+t.getRRN() +"'";
	ResultSet rs = s.executeQuery(query);
	if (rs.next()) {
	    t.setCardHolder (new CardHolder (rs.getString(1),
	                                    rs.getString(2))
					    );
	} else {
	    addFinancial(t);
	}
	rs.close();
    }
    private void syncFinancial (FinancialTransaction t)
	throws SQLException
    {
	Statement s = sqlConnection.createStatement();
	StringBuffer buf = new StringBuffer ("UPDATE journal SET");
	CardHolder ch = t.getCardHolder();
	String sep = "";
	if (ch != null) {
	    buf.append (" pan='" + ch.getPAN() + "'");
	    sep = ",";
	    if (ch.getEXP() != null) 
		buf.append (sep + "exp='" + ch.getEXP() +"'");
	}
	buf.append (sep + "amount=" + t.getAmount());
	buf.append (" WHERE rrn='" + t.getRRN() + "'");
	System.out.println ("sync: "+buf.toString());
   	s.executeUpdate (buf.toString());
   	s.close();
    }
    private void addFinancial (FinancialTransaction t) throws SQLException
    {
	Statement s = sqlConnection.createStatement();
   	s.executeUpdate (
	    "INSERT INTO journal (rrn) VALUES ('"
	    +t.getRRN() +"')");
   	s.close();
    }
    public void sync (CardTransaction t) throws IOException {
	try {
	    if (t instanceof FinancialTransaction)
		syncFinancial ((FinancialTransaction) t);
	} catch (Exception e) {
	    LogEvent evt = new LogEvent (this, "sync", t);
	    evt.addMessage (e);
	    Logger.log (evt);
	    throw new IOException (e.toString());
	}
    }
    public void fetch (CardTransaction t) throws IOException {
	try {
	    if (t instanceof FinancialTransaction)
		fetchFinancial ((FinancialTransaction) t);
	} catch (Exception e) {
	    LogEvent evt = new LogEvent (this, "fetch", t);
	    evt.addMessage (e);
	    Logger.log (evt);
	    throw new IOException (e.toString());
	}
    }
}
