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
package qsp.rmiconnector;

import mx4j.connector.rmi.RMIConnector;
import mx4j.connector.rmi.jrmp.JRMPConnector;
import mx4j.connector.RemoteMBeanServer;

import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Properties;

/**
 *  RMIConnector test client
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 */
public class Test {
    public static void main(String[] args) throws Exception
        {
                // Create an RMIConnector, passing it the JNDI name of the JRMP Adaptor
                String jndiName = "jrmp";
                Properties prop = System.getProperties();
                /*
                Hashtable prop = new Hashtable();
                prop.put("java.naming.factory.initial","com.sun.jndi.rmi.registry.RegistryContextFactory");
                prop.put("java.naming.provider.url","rmi://localhost:1099");
                */
                RMIConnector connector = new JRMPConnector();
                connector.connect(jndiName,prop);

                RemoteMBeanServer server = connector.getRemoteMBeanServer();
                
                //get Attribute value
                ObjectName objName = new ObjectName("QSP:type=server,name=server");
                int jobcount = ((Integer)server.getAttribute(objName,"JobCount")).intValue();
                System.out.println("jobcount:" + jobcount);
                int port = ((Integer)server.getAttribute(objName,"Port")).intValue();
                System.out.println("port:" + port);
                
                //invoke method
                server.invoke(objName,"resetCounters",new Object[0],new String[0]);

        }
}

