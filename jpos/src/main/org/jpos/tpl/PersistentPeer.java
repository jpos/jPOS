package org.jpos.tpl;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Id$
 */
public interface PersistentPeer {
    public void setPersistentEngine (PersistentEngine engine);
    public void create (Object obj) throws SQLException;
    public void load   (Object obj) throws SQLException;
    public void update (Object obj) throws SQLException;
    public void remove (Object obj) throws SQLException;
}
