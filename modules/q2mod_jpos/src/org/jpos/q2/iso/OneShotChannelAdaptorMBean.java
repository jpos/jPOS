/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.iso;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @author Thomas L. Kjeldsen
 * @version $Revision: 2245 $ $Date: 2006-01-31 08:27:10 -0200 (Tue, 31 Jan 2006) $
 */
public interface OneShotChannelAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setInQueue(java.lang.String in) ;

  java.lang.String getInQueue() ;

  void setOutQueue(java.lang.String out) ;

  java.lang.String getOutQueue() ;

  void setHost(java.lang.String host) ;

  java.lang.String getHost() ;

  void setPort(int port) ;

  int getPort() ;

  void setSocketFactory(java.lang.String sFac) ;

  java.lang.String getSocketFactory() ;

}
