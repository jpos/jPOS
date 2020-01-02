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

package org.jpos.tpl;

import org.jpos.iso.ISODate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SQL string manipulation helper class
 * @author apr@cs.com.uy
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public class RowMap {
    protected Map map;
    public RowMap () {
        map = new HashMap();
    }
    public void set (String name, String value) {
        map.put (name, value != null ? "'"+escape(value)+"'" : "null");
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
    public Map getMap() {
        return map;
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
    public String escape (String s) {
        if (s.indexOf ("'") != -1 ) {
            StringBuffer sb = new StringBuffer(s.length() + 1); // at least 1
            char c;
            for(int i=0; i < s.length(); i++ ) {
                c = s.charAt (i);
                if (c == '\'' || c == '\\')
                    sb.append ('\\');
                sb.append(c);
            }
            s = sb.toString();
        }
        return s;
    }
}

