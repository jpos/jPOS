package org.jpos.dblog;

/**
 * <p>Title: LogHandler</p>
 * <p>Description: Wrapper class for the JposMessageLogDao data access object</p>
 * @author Jeff Gordy
 * @version 0.1
 */

import java.sql.*;

/**
 * Wrapper for the JposMessageLogDao data access object
 */

public class LogHandler extends JposMessageLogDao {
  // private static String sql = "SELECT max(detailId) FROM jposMessageDetail";
  private static String sql = "SELECT max(msgId) FROM jposMessageLog";
  private static PreparedStatement stmt = null;

  public LogHandler() {
    super();
  }

  /**
   * getNextID - grabs the next available message id from the table
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
