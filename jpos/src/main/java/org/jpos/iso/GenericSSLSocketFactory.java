/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

package org.jpos.iso;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.SimpleLogSource;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * <code>SunJSSESocketFactory</code> is used by BaseChannel and ISOServer
 * in order to provide hooks for SSL implementations.
 *
 * @version $Revision$ $Date$
 * @author  Bharavi Gade
 * @author Alwyn Schoeman
 * @since   1.3.3
 */
public class GenericSSLSocketFactory 
        extends SimpleLogSource 
        implements ISOServerSocketFactory,ISOClientSocketFactory, Configurable
{ 
    /** Default constructor. */
    public GenericSSLSocketFactory() {}

    private SSLContext sslc=null;
    private SSLServerSocketFactory  serverFactory=null;
    private SSLSocketFactory socketFactory=null;

    private String keyStore=null;
    private String password=null;
    private String keyPassword=null;
    private String serverName;
    private boolean clientAuthNeeded=false;
    private boolean serverAuthNeeded=false;
    private String[] enabledCipherSuites;
    private String[] enabledProtocols;

    private Configuration cfg;

    /**
     * Sets the keystore file path.
     * @param keyStore the keystore file path
     */
    public void setKeyStore(String keyStore){
        this.keyStore=keyStore;  
    }

    /**
     * Sets the keystore password.
     * @param password the keystore password
     */
    public void setPassword(String password){
        this.password=password;  
    }

    /**
     * Sets the key entry password.
     * @param keyPassword the key entry password
     */
    public void setKeyPassword(String keyPassword){
        this.keyPassword=keyPassword;  
    }

    /**
     * Sets the expected server name for SNI/hostname verification.
     * @param serverName the expected server name
     */
    public void setServerName(String serverName){
        this.serverName=serverName;  
    }

    /**
     * Sets whether client authentication is required.
     * @param clientAuthNeeded true if client authentication is required
     */
    public void setClientAuthNeeded(boolean clientAuthNeeded){
        this.clientAuthNeeded=clientAuthNeeded;  
    }

    /**
     * Sets whether server authentication is required.
     * @param serverAuthNeeded true if server authentication is required
     */
    public void setServerAuthNeeded(boolean serverAuthNeeded){
        this.serverAuthNeeded=serverAuthNeeded;  
    }

    private TrustManager[] getTrustManagers(KeyStore ks)
        throws GeneralSecurityException {
        if (serverAuthNeeded) {
            TrustManagerFactory tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
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
            FileInputStream fis = new FileInputStream (new File (keyStore));
            ks.load(fis,password.toCharArray());
            fis.close();
            KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            km.init( ks, keyPassword.toCharArray() );
            KeyManager[] kma = km.getKeyManagers();
            TrustManager[] tma = getTrustManagers( ks );
            SSLContext sslc = SSLContext.getInstance( "TLS" );
            sslc.init( kma, tma, new SecureRandom() );
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
        SSLServerSocket serverSocket = (SSLServerSocket) socket;
        serverSocket.setNeedClientAuth(clientAuthNeeded);
        if (enabledCipherSuites != null && enabledCipherSuites.length > 0) {
            serverSocket.setEnabledCipherSuites(enabledCipherSuites);
        }
        if (enabledProtocols != null && enabledProtocols.length > 0) {
            serverSocket.setEnabledProtocols(enabledProtocols);
        }
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


        Certificate[] certs = session.getPeerCertificates();
        if (certs==null || certs.length==0)
            throw new SSLPeerUnverifiedException("No server certificates found");

        if (!(certs[0] instanceof X509Certificate cert))
            throw new SSLPeerUnverifiedException("Server certificate is not X.509");

        String cn = getCN(cert);
        if (!serverName.equalsIgnoreCase(cn)) {
            throw new SSLPeerUnverifiedException("Invalid SSL server name. "+
                    "Expected '" + serverName +
                    "', got '" + cn + "'");
        }
    }

    /**
     * Extracts the Common Name (CN) from an X.509 certificate's subject DN
     * using the standard {@link X500Principal} and {@link LdapName} APIs,
     * which correctly handle quoting and escaping per RFC 2253.
     *
     * @param cert  an X.509 certificate
     * @return the value of the "Common Name" field, or null if not present.
     */
    private String getCN(X509Certificate cert) throws SSLPeerUnverifiedException {
        try {
            X500Principal principal = cert.getSubjectX500Principal();
            LdapName ln = new LdapName(principal.getName(X500Principal.RFC2253));
            for (Rdn rdn : ln.getRdns()) {
                if ("CN".equalsIgnoreCase(rdn.getType())) {
                    return rdn.getValue().toString();
                }
            }
            return null;
        } catch (InvalidNameException e) {
            throw new SSLPeerUnverifiedException("Invalid subject DN: " + e.getMessage());
        }
    }

    /**
     * Returns the configured keystore file path.
     * @return the keystore file path
     */
    public String getKeyStore() {
        return keyStore;
    }

    // Have custom hooks get passwords
    // You really need to modify these two implementations
    /**
     * Returns the keystore password.
     * @return the keystore password
     */
    protected String getPassword() {
        return System.getProperty("jpos.ssl.storepass", "password");
    }

    /**
     * Returns the key entry password.
     * @return the key entry password
     */
    protected String getKeyPassword() {
        return System.getProperty("jpos.ssl.keypass", "password");
    }

    /**
     * Returns the expected server name.
     * @return the expected server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns whether client authentication is required.
     * @return true if client authentication is required
     */
    public boolean getClientAuthNeeded() {
        return clientAuthNeeded;
    }

    /**
     * Returns whether server authentication is required.
     * @return true if server authentication is required
     */
    public boolean getServerAuthNeeded() {
        return serverAuthNeeded;
    }
    
    /**
     * Sets the list of enabled cipher suites.
     * @param enabledCipherSuites the cipher suite names to enable
     */
    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    /**
     * Returns the enabled cipher suites, or null if using defaults.
     * @return array of enabled cipher suite names, or null
     */
    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }


    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
        keyStore = cfg.get("keystore");
        clientAuthNeeded = cfg.getBoolean("clientauth");
        serverAuthNeeded = cfg.getBoolean("serverauth");
        serverName = cfg.get("servername");
        password = cfg.get("storepassword", null);
        keyPassword = cfg.get("keypassword", null);
        enabledCipherSuites = cfg.getAll("addEnabledCipherSuite");
        enabledProtocols = cfg.getAll("addEnabledProtocol");
    }
    /**
     * Returns the current configuration.
     * @return the current {@link org.jpos.core.Configuration}
     */
    public Configuration getConfiguration() {
        return cfg;
    }
}
