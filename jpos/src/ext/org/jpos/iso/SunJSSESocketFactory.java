/*
 * Copyright (c) 2004 jPOS.org 
 *
 * See terms of license at http://jpos.org/license.html
 *
 */

package org.jpos.iso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.ReConfigurable;
import org.jpos.util.SimpleLogSource;

/**
 * <code>SunJSSESocketFactory</code> is used by BaseChannel and ISOServer
 * in order to provide hooks for SSL implementations.
 *
 * @version $Revision$ $Date$
 * @author  Bharavi Gade
 * @author Alwyn Schoeman
 * @since   1.3.3
 */
public class SunJSSESocketFactory 
        extends SimpleLogSource 
        implements ISOServerSocketFactory,ISOClientSocketFactory,ReConfigurable
{ 

    private SSLContext sslc=null;
    private SSLServerSocketFactory  serverFactory=null;
    private SSLSocketFactory socketFactory=null;

    private String keyStore=null;
    private String password=null;
    private String keyPassword=null;
    private boolean clientAuthNeeded=false;
    private Configuration cfg;

    public void setKeyStore(String keyStore){
        this.keyStore=keyStore;  
    }

    public void setPassword(String password){
        this.password=password;  
    }

    public void setKeyPassword(String keyPassword){
        this.keyPassword=keyPassword;  
    }

    static
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider()); 
    }
       
    /**
     * Create a SSLSocket Context
     * @return the SSLContext
     * @returns null if exception occurrs
     */
    private SSLContext getSSLContext() throws ISOException {
        if(password==null)  password=getPassword();
        if(keyPassword ==null)  keyPassword=getKeyPassword();
        if(keyStore==null)keyStore=System.getProperty("user.home")+File.separator+".keystore";

        try{
            KeyStore ks = KeyStore.getInstance( "JKS" );
            ks.load( new FileInputStream( new File( keyStore) ),password.toCharArray());
            KeyManagerFactory km = KeyManagerFactory.getInstance( "SunX509"); 
            km.init( ks, keyPassword.toCharArray() );
            KeyManager[] kma = km.getKeyManagers();                        
            TrustManagerFactory tm = TrustManagerFactory.getInstance("SunX509" );
            tm.init( ks ); 
            TrustManager[] tma = tm.getTrustManagers(); 
            SSLContext sslc = SSLContext.getInstance( "SSL" ); 
            sslc.init( kma, tma, SecureRandom.getInstance( "SHA1PRNG" ) ); 
       
            return sslc;
        } catch(Exception e) {
            throw new ISOException (e);
        } finally { 
            password=null;
            keyPassword=null;
        }
    }

    /**
     * Create a socket factory
     * @return the socket factory
     * @exception ISOException if an error occurs during server socket
     * creation
     */
    protected SSLServerSocketFactory createServerSocketFactory() 
        throws ISOException
    {
        if(sslc==null) sslc=getSSLContext();
        return sslc.getServerSocketFactory();
    }
        
    /**
     * Create a socket factory
     * @return the socket factory
     * @exception ISOException if an error occurs during server socket
     * creation
     */
    protected SSLSocketFactory createSocketFactory() 
        throws ISOException
    {
        if(sslc==null) sslc=getSSLContext();
        return sslc.getSocketFactory();
    }
    
   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception IOException should an I/O error occurs during 
    * @exception ISOException should an error occurs during 
    * creation
    */
    public ServerSocket createServerSocket(int port) 
        throws IOException, ISOException
    {
        if(serverFactory==null) serverFactory=createServerSocketFactory();
        ServerSocket socket = serverFactory.createServerSocket(port);
        ((SSLServerSocket) socket).setNeedClientAuth(clientAuthNeeded);
        return socket;
    }
    
    /**
     * Create a client socket connected to the specified host and port.
     * @param  host   the host name
     * @param  port   the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @exception ISOException should any other error occurs
     */
    public Socket createSocket(String host, int port) 
        throws IOException, ISOException
    {
        if(socketFactory==null) socketFactory=createSocketFactory();
        return socketFactory.createSocket(host,port);
    }

    //Have custom hooks get passwords
    //You really neede to modify these two implementations
    //We can make use of 
    //PASSWORD_PROPERTY="jpos.ssl.password";
    //KEYPASSWORD_PROPERTY="jpos.ssl.keypassword";
    protected String getPassword()
    {
        return "password";
    }

    protected String getKeyPassword()
    {
        return "password";
    }

    /**
     * @see org.jpos.core.Configurable#setConfiguration(org.jpos.core.Configuration)
     */
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        keyStore = cfg.get("keystore");
        clientAuthNeeded = cfg.getBoolean("clientauth");
    }
}
