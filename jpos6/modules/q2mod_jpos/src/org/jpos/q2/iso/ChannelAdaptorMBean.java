/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.iso;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 2241 $ $Date: 2006-01-23 11:27:32 -0200 (Mon, 23 Jan 2006) $
 */
public interface ChannelAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setReconnectDelay(long delay) ;

  long getReconnectDelay() ;

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
