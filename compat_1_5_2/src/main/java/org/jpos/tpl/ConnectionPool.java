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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

/** A class for preallocating, recycling, and managing
 *  JDBC connections.
 *  <P>
 *  Taken from Core Servlets and JavaServer Pages
 *  from Prentice Hall and Sun Microsystems Press,
 *  http://www.coreservlets.com/.
 *  &copy; 2000 Marty Hall; may be freely used or adapted.
 *  @author Rajal Shah
 *  @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
public class ConnectionPool implements Runnable, LogSource, Configurable {
    Configuration cfg;
    Logger logger;
    String realm;

    private String driver, url, username, password;
    private int maxConnections;
    private boolean waitIfBusy;
    private Vector availableConnections, busyConnections;
    private boolean connectionPending = false;

   /**
    * no args constructor
    */
    public ConnectionPool () {
        super();
    }

    public void setConfiguration (Configuration cfg)
        throws ConfigurationException
    {
        this.cfg = cfg;
        initEngine();
    }

    private void initEngine () throws ConfigurationException {
        initJDBC();
    }

    private void initJDBC() throws ConfigurationException {
        try {
            Class.forName(cfg.get("jdbc.driver")).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException (e);
        }

        driver = cfg.get("jdbc.driver");
        url    = cfg.get("jdbc.url");
        username = cfg.get("jdbc.user");
        password = cfg.get("jdbc.password");
        int initialConnections = cfg.getInt("initial-connections");
        maxConnections = cfg.getInt("max-connections");
        waitIfBusy = cfg.getBoolean("wait-if-busy");

        if (initialConnections > maxConnections) {
            initialConnections = maxConnections;
        }
        availableConnections = new Vector(initialConnections);
        busyConnections = new Vector();
        for(int i=0; i<initialConnections; i++) {
            try {
                availableConnections.addElement(makeNewConnection());
            } catch(SQLException e) {
                throw new ConfigurationException(e);
            }
        }
    }

   /**
    * To invoke the Connection Pool object directly without jPOS.
    */
    public ConnectionPool(String driver, String url,
                        String username, String password,
                        int initialConnections,
                        int maxConnections,
                        boolean waitIfBusy)
        throws SQLException
    {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
        this.waitIfBusy = waitIfBusy;
        if (initialConnections > maxConnections) {
            initialConnections = maxConnections;
        }
        availableConnections = new Vector(initialConnections);
        busyConnections = new Vector();
        for(int i=0; i<initialConnections; i++) {
            availableConnections.addElement(makeNewConnection());
        }
    }

    public synchronized Connection getConnection()
        throws SQLException
    {
        if (!availableConnections.isEmpty()) {
            Connection existingConnection =
                (Connection)availableConnections.lastElement();
            int lastIndex = availableConnections.size() - 1;
            availableConnections.removeElementAt(lastIndex);
            // If connection on available list is closed (e.g.,
            // it timed out), then remove it from available list
            // and repeat the process of obtaining a connection.
            // Also wake up threads that were waiting for a
            // connection because maxConnection limit was reached.
            if (existingConnection.isClosed()) {
                notifyAll(); // Freed up a spot for anybody waiting
                return getConnection();
            } else {
                busyConnections.addElement(existingConnection);
                return existingConnection;
            }
        } else {
            // Three possible cases:
            // 1) You haven't reached maxConnections limit. So
            //    establish one in the background if there isn't
            //    already one pending, then wait for
            //    the next available connection (whether or not
            //    it was the newly established one).
            // 2) You reached maxConnections limit and waitIfBusy
            //    flag is false. Throw SQLException in such a case.
            // 3) You reached maxConnections limit and waitIfBusy
            //    flag is true. Then do the same thing as in second
            //    part of step 1: wait for next available connection.

            if (totalConnections() < maxConnections && !connectionPending) {
                makeBackgroundConnection();
            } else if (!waitIfBusy && !connectionPending) {
                throw new SQLException("Connection limit reached");
            }
            // Wait for either a new connection to be established
            // (if you called makeBackgroundConnection) or for
            // an existing connection to be freed up.
            try {
                wait();
            } catch (InterruptedException ie) {}
            // Someone freed up a connection, so try again.
            return getConnection();
        }
    }

    // You can't just make a new connection in the foreground
    // when none are available, since this can take several
    // seconds with a slow network connection. Instead,
    // start a thread that establishes a new connection,
    // then wait. You get woken up either when the new connection
    // is established or if someone finishes with an existing
    // connection.
    private void makeBackgroundConnection() {
        connectionPending = true;
        try {
            Thread connectThread = new Thread(this,"ConnectionPool-connect");
            connectThread.start();
        } catch(OutOfMemoryError oome) {
        // Give up on new connection
        }
    }

    public void run() {
        Connection connection = null;
        while (connection == null) {
            try {
                connection = makeNewConnection();
                synchronized(this) {
                    availableConnections.addElement(connection);
                    connectionPending = false;
                    notifyAll();
                }
            } catch(Exception e) { // SQLException or OutOfMemory
                LogEvent evt = new LogEvent (this, "error");
                evt.addMessage ("An error occurred while trying to make a background connection");
                evt.addMessage (e);
                Logger.log (evt);
                try {
                    Thread.sleep (3000); // don't get too crazy about retrying
                } catch (InterruptedException ie) {
                    // this one, we don't care.
                }
            }
        }
    }

    // This explicitly makes a new connection. Called in
    // the foreground when initializing the ConnectionPool,
    // and called in the background when running.

    private Connection makeNewConnection()
        throws SQLException {
        try {
            // Load database driver if not already loaded
            Class.forName(driver);
            // Establish network connection to database
            Connection connection =
                DriverManager.getConnection(url, username, password);
            return connection;
        } catch(ClassNotFoundException cnfe) {
            // Simplify try/catch blocks of people using this by
            // throwing only one exception type.
            throw new SQLException("Can't find class for driver: " +
                                driver);
        }
    }

    public synchronized void free(Connection connection) {
        if(busyConnections.removeElement(connection)) {
            availableConnections.addElement(connection);
        }
        // Wake up threads that are waiting for a connection
        notifyAll();
    }

    public synchronized int totalConnections() {
        return availableConnections.size() + busyConnections.size();
    }

    public synchronized int getTotalConnections(){
        return availableConnections.size() + busyConnections.size();
    }

    /** Close all the connections. Use with caution:
    *  be sure no connections are in use before
    *  calling. Note that you are not <I>required</I> to
    *  call this when done with a ConnectionPool, since
    *  connections are guaranteed to be closed when
    *  garbage collected. But this method gives more control
    *  regarding when the connections are closed.
    */

    public synchronized void closeAllConnections() {
        closeConnections(availableConnections);
        availableConnections = new Vector();
        closeConnections(busyConnections);
        busyConnections = new Vector();
    }

    private void closeConnections(Vector connections) {
        try {
            for(int i=0; i<connections.size(); i++) {
                Connection connection = (Connection)connections.elementAt(i);
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch(SQLException sqle) {
            // Ignore errors; garbage collect anyhow
        }
    }

    public synchronized String toString() {
        String info =
            "ConnectionPool(" + url + "," + username + ")" +
            ", available=" + availableConnections.size() +
            ", busy=" + busyConnections.size() +
            ", max=" + maxConnections;
        return info;
    }

    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm  = realm;
    }
    public String getRealm () {
        return realm;
    }
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return ISOMUX instance with given name.
     * @throws NameRegistrar.NotFoundException;
     * @see NameRegistrar
     */
    public static ConnectionPool getConnectionPool (String name)
        throws NameRegistrar.NotFoundException
    {
        return (ConnectionPool) NameRegistrar.get ("connection.pool."+name);
    }

}
