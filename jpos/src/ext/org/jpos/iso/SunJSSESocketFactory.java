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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import javax.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
    private String serverName;
    private boolean clientAuthNeeded=false;
    private boolean serverAuthNeeded=false;

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

    public void setServerName(String serverName){
        this.serverName=serverName;  
    }

    public void setClientAuthNeeded(boolean clientAuthNeeded){
        this.clientAuthNeeded=clientAuthNeeded;  
    }

    public void setServerAuthNeeded(boolean serverAuthNeeded){
        this.serverAuthNeeded=serverAuthNeeded;  
    }

    static
    {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider()); 
    }
       
    private TrustManager[] getTrustManagers(KeyStore ks)
        throws GeneralSecurityException {
        if (serverAuthNeeded) {
            TrustManagerFactory tm = TrustManagerFactory.getInstance("SunX509" );
            tm.init( ks ); 
            return tm.getTrustManagers(); 
        } else {
            // Create a trust manager that does not validate certificate chains
            return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[] {};
                    }
                    public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };
        }
    }

    /**
     * Create a SSLSocket Context
     * @return the SSLContext
     * @returns null if exception occurrs
     */
    private SSLContext getSSLContext() throws ISOException {
        if(password==null)  password=getPassword();
        if(keyPassword ==null)  keyPassword=getKeyPassword();
        if(keyStore==null || keyStore.length()==0) {
            keyStore=System.getProperty("user.home")+File.separator+".keystore";
        }

        try{
            KeyStore ks = KeyStore.getInstance( "JKS" );
            ks.load( new FileInputStream( new File( keyStore) ),password.toCharArray());
            KeyManagerFactory km = KeyManagerFactory.getInstance( "SunX509"); 
            km.init( ks, keyPassword.toCharArray() );
            KeyManager[] kma = km.getKeyManagers();
            TrustManager[] tma = getTrustManagers( ks );
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
        SSLSocket s = (SSLSocket) socketFactory.createSocket(host,port);
        verifyHostname(s);
        return s;
    }

    /**
     * Verify that serverName and CN equals.
     *
     * <pre>
     * Origin:      jakarta-commons/httpclient
     * File:        StrictSSLProtocolSocketFactory.java
     * Revision:    1.5
     * License:     Apache-2.0
     * </pre>
     *
     * @param socket a SSLSocket value
     * @exception SSLPeerUnverifiedException  If there are problems obtaining
     * the server certificates from the SSL session, or the server host name 
     * does not match with the "Common Name" in the server certificates 
     * SubjectDN.
     * @exception UnknownHostException  If we are not able to resolve
     * the SSL sessions returned server host name. 
     */
    private void verifyHostname(SSLSocket socket)
        throws SSLPeerUnverifiedException, UnknownHostException
    {
        if (!serverAuthNeeded) {
            return; 
        }

        SSLSession session = socket.getSession();

        if (serverName==null || serverName.length()==0) {
            serverName = session.getPeerHost();
            try {
                InetAddress addr = InetAddress.getByName(serverName);
            } catch (UnknownHostException uhe) {
                throw new UnknownHostException("Could not resolve SSL " +
                                               "server name " + serverName);
            }
        }


        X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs==null || certs.length==0) 
            throw new SSLPeerUnverifiedException("No server certificates found");

        //get the servers DN in its string representation
        String dn = certs[0].getSubjectDN().getName();

        //get the common name from the first cert
        String cn = getCN(dn);
        if (!serverName.equalsIgnoreCase(cn)) {
            throw new SSLPeerUnverifiedException("Invalid SSL server name. "+
                                                 "Expected '" + serverName +
                                                 "', got '" + cn + "'");
        }
    }

    /**
     * Parses a X.500 distinguished name for the value of the 
     * "Common Name" field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to RFC 2253.
     *
     * <pre>
     * Origin:      jakarta-commons/httpclient
     * File:        StrictSSLProtocolSocketFactory.java
     * Revision:    1.5
     * License:     Apache-2.0
     * </pre>
     *
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    private String getCN(String dn) {
        int i = dn.indexOf("CN=");
        if (i == -1) {
            return null;
        }
        //get the remaining DN without CN=
        dn = dn.substring(i + 3);  
        // System.out.println("dn=" + dn);
        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\') {
                break;
            }
        }
        return dn.substring(0, i);
    }

    public String getKeyStore() {
        return keyStore;
    }

    // Have custom hooks get passwords
    // You really need to modify these two implementations
    protected String getPassword() {
        return System.getProperty("jpos.ssl.storepass", "password");
    }

    protected String getKeyPassword() {
        return System.getProperty("jpos.ssl.keypass", "password");
    }

    public String getServerName() {
        return serverName;
    }

    public boolean getClientAuthNeeded() {
        return clientAuthNeeded;
    }

    public boolean getServerAuthNeeded() {
        return serverAuthNeeded;
    }

    /**
     * @see org.jpos.core.Configurable#setConfiguration(org.jpos.core.Configuration)
     */
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        keyStore = cfg.get("keystore");
        clientAuthNeeded = cfg.getBoolean("clientauth");
        serverAuthNeeded = cfg.getBoolean("serverauth");
        serverName = cfg.get("servername");
    }
}
