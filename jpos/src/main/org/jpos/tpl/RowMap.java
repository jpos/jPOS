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
