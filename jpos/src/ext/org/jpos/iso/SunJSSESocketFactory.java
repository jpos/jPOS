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
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jpos.util.SimpleLogSource;

/**
 * <code>SunJSSESocketFactory</code> is used by BaseChannel and ISOServer
 * in order to provide hooks for SSL implementations.
 *
 * @version $Revision$ $Date$
 * @author  Bharavi Gade
 * @since   1.3.3
 */
public class SunJSSESocketFactory 
        extends SimpleLogSource 
        implements ISOServerSocketFactory,ISOClientSocketFactory
{ 

    private SSLContext sslc=null;
    private SSLServerSocketFactory  serverFactory=null;
    private SSLSocketFactory socketFactory=null;

    private String keyStore=null;
    private String password=null;
    private String keyPassword=null;

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
    }
    catch(Exception e)
    {
        throw new ISOException (e);
    }
    finally
    { 
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
        return serverFactory.createServerSocket(port);
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
}
