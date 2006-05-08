/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.iso;

/**
 * MBean interface.
 * @author Alwyn Schoeman
 * @version $Revision: 2072 $ $Date: 2004-12-07 08:21:24 -0300 (Tue, 07 Dec 2004) $
 */
public interface QServerMBean extends org.jpos.q2.QBeanSupportMBean {

  void setPort(int port) ;

  int getPort() ;

  void setPackager(java.lang.String packager) ;

  java.lang.String getPackager() ;

  void setChannel(java.lang.String channel) ;

  java.lang.String getChannel() ;

  void setMaxSessions(int maxSessions) ;

  int getMaxSessions() ;

  void setMinSessions(int minSessions) ;

  int getMinSessions() ;

  void setSocketFactory(java.lang.String sFactory) ;

  java.lang.String getSocketFactory() ;

}
