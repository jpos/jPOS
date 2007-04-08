package org.jpos.dblog.examples;

/**
 * <p>Title: jPOS Modules Example Script</p>
 *
 * <p>Description: JPOS Extensible Database Module</p>
 *
 * <p>Copyright: See terms of license at http://jpos.org/license.html</p>
 * @author Jeff Gordy
 * @version 1.0
 */

import org.jpos.iso.*;
import org.jpos.core.*;
import java.util.Date;
import java.util.Random;
import org.jpos.dblog.*;


public class Test {

  /**
   * interchange - In this we are pretending we have sent the message to an
   * outside interchange and are reading the reply off the wire.  All we do is
   * set the response code bit 39 to approved.
   *
   * @param m ISOMsg
   * @throws ISOException
   * @return ISOMsg
   */
  public static ISOMsg interchange(ISOMsg m) throws ISOException {
    ISOMsg c = (ISOMsg) m.clone();
    c.setResponseMTI();
    c.set(39, "00");
    return c;
  }

  /**
   * main - Here we create an ISOMsg object, send it to our dummy interchange,
   * and log both the original message and the result from the interchange.
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    // First we want to assign our configuration file.
    Configuration cfg = null;
    try {
      cfg = new SimpleConfiguration("/home/jgordy/test.cfg");
    } catch (Exception e) {
      System.out.println("Error with Configuration File: " + e.getMessage());
    }

    try {
      Date d = new Date();
      // This random number will be used to show unique
      // numbers over mutliple runs in the tables.
      Random r = new Random(System.currentTimeMillis());
      int somethingToTrack = r.nextInt(10000) + 1;

      // create a new simple 800 messsage
      ISOMsg m = new ISOMsg("0800");
      m.set(11, "000001");
      m.set(12, ISODate.getTime(d));
      m.set(13, ISODate.getDate(d));
      m.set(41, Integer.toString(somethingToTrack) );

      // the bit string array indicates we want to track bits 11, 12, 39, and 41
      // if we did not create a bit string the database logger object would
      // just log complete messages.
      String[] bits = {"11", "12", "39", "41"};
      // create the new databaselogger object.  Pass it our configuration file
      // for the JDBC connection, and our bit array.
      DatabaseLogger db = new DatabaseLogger(cfg, bits);

      // log the message "m" as the incoming message.  The incoming message
      // creates a new entry in the jposMessageLog table, and gets assigned a
      // unique message id.
      System.out.println("Inserting Incoming ISOMsg Object Into Database");
      db.logIncomingMessage(m);

      // here we read an ISOMsg m2 pretending it was a response from an interchange.
      System.out.println("Inserting Reply ISOMsg Object Into Database");
      ISOMsg m2 = interchange(m);
      // and we log the response as our outgoingMessage
      db.logOutgoingMessage(m2);

      // here we tell the DatabaseLogger object that we are done with it and it
      // can close all connections in the connection pool.  This should only really
      // be done whenever you shutdown your application.  Otherwise the connection pool
      // needs to remain open to actually "pool"
      db.freePool();

    } catch (ISOException e) {
      e.printStackTrace();
    } catch (java.sql.SQLException e) {
      new SQLExceptionHandler(e);
    } catch (NotFoundException e) {
      e.printStackTrace();
    }
  }
}
