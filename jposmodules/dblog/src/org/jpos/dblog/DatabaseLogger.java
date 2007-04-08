package org.jpos.dblog;

/**
 * <p>Title: DatabaseLogger</p>
 * <p>Description: Log ISO Messages to a Database</p>
 * @author Jeff Gordy
 * @version 0.1
 *
 */

import java.sql.*;
import org.jpos.tpl.ConnectionPool;
import org.jpos.iso.*;
import org.jpos.core.Configuration;

/**
 * This class can be utilized to store ISOMsg's and particular message bits
 * in a database.
 */
public class DatabaseLogger {
   private static ConnectionPool cp = null;
   private LogHandler lh = null;
   private DetailHandler dh = null;
   private Timestamp t = null;
   private JposMessageLog msg = null;
   private JposMessageDetail detail = null;
   private String[] detailBits;

  /**
   * getConnectionPool - generates a connection pool.  This is the bad part that
   * shows the need to implement configurable.
   *
   * @throws SQLException
   * @return ConnectionPool
   */
  private static ConnectionPool getConnectionPool(Configuration cfg) throws SQLException {
    String Driver = cfg.get("jdbc.driver");
    String URL = cfg.get("jdbc.url");
    String User = cfg.get("jdbc.user");
    String Password = cfg.get("jdbc.password");
    return new ConnectionPool(Driver, URL, User, Password, 1, 10, true);
  }

  /**
   * DatabaseLogger - used when tracking complete messages only.
   *
   * @throws SQLException
   * @param cfg Configuration
   */
  public DatabaseLogger(Configuration cfg) throws SQLException {
    cp = getConnectionPool(cfg);
    this.lh = new LogHandler();
    this.dh = new DetailHandler();
    this.detailBits = new String[] { "none" };
    this.updateTimestamp();
  }

  /**
   * DatabaseLogger - this constructor sets up the object to record complete
   * messages as well as seperate entries for each of the desired bits in the
   * detailBitTracking array.
   *
   * @param cfg Configuration
   * @param detailBitTracking String[]
   * @throws SQLException
   */
  public DatabaseLogger(Configuration cfg, String[] detailBitTracking) throws SQLException {
    cp = getConnectionPool(cfg);
    this.lh = new LogHandler();
    this.dh = new DetailHandler();
    this.alterDetailBits(detailBitTracking);
    this.updateTimestamp();
  }

  /**
   * freeConnection - gives the connection back to the pool.
   *
   * @param conn Connection
   * @throws SQLException
   */
  public void freeConnection(Connection conn) throws SQLException {
    cp.free(conn);
  }

  /**
   * logDetail
   *
   * @param m ISOMsg
   * @param msg JposMessageLog - used for its msgId only.  If desired you can
   * call this directly, just create a JposMessageLog object first and set its
   * message id msgID.
   * @param conn Connection - when calling directly you need to provide the connection
   * @param type String - one of the enumerated msgTypes from the table jposMessageDetail
   * @throws SQLException
   */
  public void logDetail(ISOMsg m, JposMessageLog msg, Connection conn, String type)
      throws SQLException {
    int mid = msg.getMsgId();
    if (this.detailBits[0] != "none") {
      for (int i = 0; i < this.detailBits.length; i++) {
        String bit = this.detailBits[i];
        int b = Integer.parseInt(bit);
        if (m.hasField(b)) {
          String value = m.getString(b);
          int detail_id = this.dh.getNextID(conn);
          this.detail = new JposMessageDetail();
          this.detail.setAll(detail_id, mid, bit, value, type);
          this.dh.create(conn, this.detail);
        }
      }
    }
  }

  /**
   * alterDetailBits - this function allows dynamic changing of bits to track.
   *
   * @param detailBitTracking String[]
   */
  public void alterDetailBits(String[] detailBitTracking) {
    this.detailBits = detailBitTracking;
  }

  /**
   * logIncomingMessage - logs the original message received by the application
   *
   * @param m ISOMsg
   * @throws SQLException
   */
  public void logIncomingMessage(ISOMsg m) throws SQLException {
    Connection conn = cp.getConnection();
    this.updateTimestamp();
    this.msg = new JposMessageLog();
    int msg_id = lh.getNextID(conn);
    this.msg.setAll(msg_id, this.dumpMessage(m), "", "", "", t);
    this.lh.create(conn, msg);
    this.logDetail(m, this.msg, conn, "incomingMsg");
    this.freeConnection(conn);
  }

  /**
   * logTransformMessage - logs an optional transformation of the incoming message
   *
   * @param m ISOMsg
   * @throws SQLException
   * @throws NotFoundException
   */
  public void logTransformMessage(ISOMsg m)
      throws SQLException, NotFoundException {
    Connection conn = cp.getConnection();
    this.msg.setTransformMsg(this.dumpMessage(m));
    this.lh.save(conn, msg);
    this.logDetail(m, this.msg, conn, "transformMsg");
    this.freeConnection(conn);
  }

  /**
   * logReplyMessage - logs an optional reply to the incoming or transformation message
   *
   * @param m ISOMsg
   * @throws SQLException
   * @throws NotFoundException
   */
  public void logReplyMessage(ISOMsg m)
      throws SQLException, NotFoundException {
    Connection conn = cp.getConnection();
    this.msg.setReplyMsg(this.dumpMessage(m));
    this.lh.save(conn, msg);
    this.logDetail(m, this.msg, conn, "replyMsg");
    this.freeConnection(conn);
  }

  /**
   * logOutgoingMessage - logs an optional outoing message
   *
   * @param m ISOMsg
   * @throws SQLException
   * @throws NotFoundException
   */
  public void logOutgoingMessage(ISOMsg m)
       throws SQLException, NotFoundException {
    Connection conn = cp.getConnection();
    this.msg.setOutgoingMsg(this.dumpMessage(m));
    this.lh.save(conn, msg);
    this.logDetail(m, this.msg, conn, "outgoingMsg");
    this.freeConnection(conn);
  }

  /**
   * getMessageLog - In case you are curious
   *
   * @return JposMessageLog
   */
  public JposMessageLog getMessageLog() {
    return this.msg;
  }

  /**
   * getMessageDetail - Once again for the curious
   *
   * @return JposMessageDetail
   */
  public JposMessageDetail getMessageDetail() {
    return this.detail;
  }

  /**
   * getPoolConnection - this will return a connection from the connection pool.
   * This is generally only useful if you wish to call the logDetail method directly
   * to bypass the complete message logging.
   *
   * @throws SQLException
   * @return Connection
   */
  public Connection getPoolConnection() throws SQLException {
    return cp.getConnection();
  }

  /**
   * freePool - closes all open database connections.
   */
  public void freePool() {
    cp.closeAllConnections();
  }

  /**
   * updateTimestamp
   */
  private void updateTimestamp() {
    t = new Timestamp(System.currentTimeMillis());
  }

  /**
   * dumpMessage - generates an xml tag string for the message
   *
   * @param m ISOMsg
   * @return String
   */
  private String dumpMessage(ISOMsg m) {
    StringBuffer s = new StringBuffer();
    s.append("<isomsg>\n");
    for (int i = 0; i < 128; i++) {
      if (m.hasField(i)) {
        String f = m.getString(i);
        s.append("<field id=\"" + i + "\" value=\"" + f + "\" />\n");
      }
    }
    s.append("</isomsg>");
    return s.toString();
  }


}
