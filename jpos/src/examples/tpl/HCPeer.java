package tpl;

import java.sql.*;
import java.util.*;
import org.jpos.tpl.*;

/**
 * HotCard Peer
 */
public class HCPeer implements PersistentPeer {
    PersistentEngine engine = null;

    public HCPeer () {
	super();
    }
    public HCPeer (PersistentEngine engine) {
	super();
	setPersistentEngine (engine);
    }

    // =============================================================
    // PersistentPeer implementation
    // =============================================================
    public void setPersistentEngine (PersistentEngine engine) {
	this.engine = engine;
    }
    public void create (Object obj) throws SQLException {
	HC hc = (HC) obj;
	engine.executeUpdate (getCreateSql (hc));
    }
    public void load (Object obj) throws SQLException {
	// nothing to do, 'pan' field is already loaded
    }
    public void update (Object obj) throws SQLException {
	HC hc = (HC) obj;
	engine.executeUpdate (getUpdateSql (hc));
    }
    public void remove (Object obj) throws SQLException {
	HC hc = (HC) obj;
	engine.executeUpdate (getRemoveSql (hc.getPan()));
    }

    // =============================================================
    // Finders
    // =============================================================
    public HC findByPan (String pan) 
	throws	SQLException,
		NotFoundException
    {
	ResultSet rs = null;
	try {
	    rs = engine.executeQuery (getSelectSql (pan));
	    if (!rs.next()) 
		throw new 
		    NotFoundException (pan + " not found");
	    return new HC (pan);
	} finally {
	    if (rs != null)
		rs.close();
	}
    }

    public Collection findByRange (String initialPan, String finalPan) 
	throws SQLException
    {
	Collection c = new ArrayList();
	ResultSet rs = null;
	try {
	    rs = engine.executeQuery (getSelectRangeSql (initialPan, finalPan));
	    while (rs.next()) {
		HC hc = new HC();
		load (hc, rs);
		c.add (hc);
	    }
	} finally {
	    if (rs != null)
		rs.close();
	}
	return c;
    }

    // =============================================================
    // private helper methods
    // =============================================================
    private String getCreateSql (HC o) {
	return "INSERT INTO nf (pan) VALUES ('" + o.getPan() + "')";
    }
    private String getSelectSql (String pan) {
	return "SELECT (pan) FROM nf WHERE pan = '" + pan + "'";
    }
    private String getRemoveSql (String pan) {
	return "DELETE FROM nf WHERE pan = '" + pan + "'";
    }
    private String getUpdateSql (HC o) {
	// sin sentido en HC, for demo purposes...
	return "UPDATE nf SET pan = '" + o.getPan() + 
	    "' WHERE pan='" + o.getPan()+ "'";
    }
    private String getSelectRangeSql (String initialPan, String finalPan) {
	return "SELECT (pan) FROM nf WHERE pan >= '" + initialPan + "'"
	+ " AND pan <= '" + finalPan + "'";
    }
    private void load (Object obj, ResultSet rs) throws SQLException {
	HC hc = (HC) obj;
	hc.setPan (rs.getString(1));
    }
}

