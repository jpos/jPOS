package org.jpos.tpl;

import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Date;
import java.math.BigDecimal;
import org.jpos.iso.ISODate;

/**
 * SQL string manipulation helper class
 * @author apr@cs.com.uy
 * @version $Id$
 */
public class RowMap {
    Map map;
    public RowMap () {
	map = new Hashtable();
    }
    public void set (String name, String value) {
	map.put (name, value != null ? "'"+value+"'" : "null");
    }
    public void set (String name, int value) {
	map.put (name, Integer.toString (value));
    }
    public void set (String name, long value) {
	map.put (name, Long.toString (value));
    }
    public void set (String name, BigDecimal value) {
	map.put (name, value.toString());
    }
    public void set (String name, Date d) {
	map.put (name, d == null ? "null" : 
	    "'" + ISODate.formatDate (d, "yyyy-MM-dd HH:mm:ss") + "'");
    }
    public String getInsertSql (String tableName) {
	StringBuffer columns = new StringBuffer();
	StringBuffer values  = new StringBuffer();
	Iterator iter = map.entrySet().iterator();
	boolean first = true;
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    if (!first) {
		columns.append (',');
		values.append  (',');
	    } else
		first = false;
	    columns.append (entry.getKey());
	    values.append (entry.getValue());
	}
	return "INSERT INTO "+tableName+ " (" + columns.toString()
	      +") VALUES (" + values.toString() + ")";
    }
    public String getUpdateSql (String tableName, String where) {
	StringBuffer sb = new StringBuffer();
	Iterator iter = map.entrySet().iterator();
	boolean first = true;
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry) iter.next();
	    if (!first) 
		sb.append (',');
	    else
		first = false;
	    sb.append (entry.getKey());
	    sb.append ('=');
	    sb.append (entry.getValue());
	}
	return "UPDATE "+tableName+ " SET "+sb.toString() + " WHERE " + where;
    }
}
