/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.qbean;

/**
 * MBean interface.
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @version $Revision: 1.2 $ $Date: 2004/04/10 16:39:59 $
 */
public interface NamingMBean extends org.jpos.q2.QBeanSupportMBean {

  void init() ;

  void start() ;

  void stop() ;

  void destroy() ;

  void setPort(int port) ;

  int getPort() ;

}
