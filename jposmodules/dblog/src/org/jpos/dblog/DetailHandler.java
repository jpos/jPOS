package org.jpos.dblog;

/**
 * <p>Title: DetailHandler</p>
 * <p>Description: Wrapper class for the Data access object JposMessageDetailDao</p>
 * @author Jeff Gordy
 * @version 0.1
 */

import java.sql.*;

/**
 * Wrapper for the JposMessageDetailDao data access object
 */

public class DetailHandler extends JposMessageDetailDao {
  private static String sql = "SELECT max(detailId) FROM jposMessageDetail";
  private static PreparedStatement stmt = null;

  public DetailHandler() {
    super();
  }

  /**
   * getNextID - grabs the next availble detail id from the table.
   *
   * @param conn Connection
   * @throws SQLException
   * @return int
   */
  public synchronized int getNextID(Connection conn) throws SQLException {
      int id = 0;

      try {
          stmt = conn.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery();
          rs.next();
          id = rs.getInt(1);
          rs.close();
      } finally {
          if (stmt != null)
              stmt.close();
      }

      return ++id;
}


}
