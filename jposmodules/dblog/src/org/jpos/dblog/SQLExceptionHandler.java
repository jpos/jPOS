package org.jpos.dblog;

/**
 * <p>Title: jPOS Contribution</p>
 * <p>Description: JPOS Extensible Database Module</p>
 * <p>Copyright: See terms of license at http://jpos.org/license.html</p>
 * <p>Company: </p>
 * @author Jeff Gordy
 * @version 1.0
 */

import java.sql.SQLException;
import java.io.*;

public class SQLExceptionHandler {
  public SQLExceptionHandler(SQLException e) {
    System.out.println("\n--- SQL Exception Caught ---\n");
    while (e != null) {
      System.out.println("Message:     " + e.getMessage() );
      System.out.println("SQLState:    " + e.getSQLState() );
      System.out.println("ErrorCode:   " + e.getErrorCode() );
      e = e.getNextException();
      System.out.println("");
    }
  }

  public SQLExceptionHandler(PrintStream p, SQLException e) {
    p.println("\n--- SQL Exception Caught ---\n");
    while (e != null) {
      p.println("Message:     " + e.getMessage() );
      p.println("SQLState:    " + e.getSQLState() );
      p.println("ErrorCode:   " + e.getErrorCode() );
      e = e.getNextException();
      p.println("");
    }
  }

}
