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

package qsp.connectionpool;

import org.jpos.core.Configuration;
import org.jpos.core.ReConfigurable;
import org.jpos.core.ConfigurationException;
import org.jpos.util.NameRegistrar;
import org.jpos.util.Logger;
import org.jpos.util.Loggeable;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.tpl.ConnectionPool;

import java.io.PrintStream;


public class Test implements Runnable, ReConfigurable, LogSource, Loggeable {
    Configuration cfg;
    Logger logger;
    String realm;
    public ConnectionPool connectionPool = null;


    public void run() {
      NameRegistrar.register (getRealm(), this);
      LogEvent evt = new LogEvent(this, getRealm(), this);
      logger.log (evt);

      //this is where you do the work..

      //The following code is a simple example to show you the pooling effect.
      //In most cases, you would be spawning separate threads and sharing
      //the pool amongst all the threads instead.

      //You can get connections from the pool,
      // using connectionPool.getConnection() method.
      //if wait-if-busy = false, a SQLException is thrown when the
      //pool limit is reached.
      //if wait-if-busy = true, then the getConnection() will block, till a
      //connection is available.

      java.sql.Connection conn1 = null;
      java.sql.Connection conn2 = null;
      java.sql.Connection conn3 = null;
      java.sql.Connection conn4 = null;
      try {
        conn1 = connectionPool.getConnection();
        conn2 = connectionPool.getConnection();
        conn3 = connectionPool.getConnection();
        conn4 = connectionPool.getConnection();
      } catch(java.sql.SQLException e) {
        //If waitIfBusy is false, then SQLException is thrown on reaching the pool limit
        logger.log(new LogEvent(this, getRealm(), e));
     } finally {
        //free all connections
        if(connectionPool != null) {
          if(conn1 != null) connectionPool.free(conn1);
          if(conn2 != null) connectionPool.free(conn2);
          if(conn3 != null) connectionPool.free(conn3);
          if(conn4 != null) connectionPool.free(conn4);
        }
      }
    }

   /**
    * implements [Re]Configurable
    * @param cfg Configuration object (called by QSP)
    */
    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;


        /** Setup Connection Pooling
          */
        try {
//          connectionPool = (ConnectionPool)
//              NameRegistrar.get ("connection.pool."+cfg.get("connection-pool"));
          connectionPool = ConnectionPool.getConnectionPool(cfg.get("connection-pool"));
        } catch (Exception e) {
          throw new ConfigurationException(e);
        }
    }

    public void dump (PrintStream p, String indent) {
        String inner = indent + "   ";
        p.println (indent + "<test>");
        p.println (inner + "   <connection-pool>"+connectionPool+"</connection-pool>");
        p.println (indent + "</test>");
    }

    // LogSource implementation - setLogger
    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public Logger getLogger() {
        return logger;
    }
    public String getRealm () {
        return realm;
    }
}
