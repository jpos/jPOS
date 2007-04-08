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
        throws  SQLException,
                NotFoundException
    {
        ResultSet rs = null;
        Connection conn = engine.getConnection();
        try {
            rs = engine.executeQuery (getSelectSql (pan), conn);
            if (!rs.next()) 
                throw new 
                    NotFoundException (pan + " not found");
            return new HC (pan);
        } finally {
            if (rs != null)
                rs.close();
            engine.releaseConnection (conn);
        }
    }

    public Collection findByRange (String initialPan, String finalPan) 
        throws SQLException
    {
        Collection c = new ArrayList();
        ResultSet rs = null;
        Connection conn = engine.getConnection();
        try {
            rs = engine.executeQuery 
                (getSelectRangeSql (initialPan, finalPan), conn);
            while (rs.next()) {
                HC hc = new HC();
                load (hc, rs);
                c.add (hc);
            }
        } finally {
            if (rs != null)
                rs.close();
            engine.releaseConnection (conn);
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

