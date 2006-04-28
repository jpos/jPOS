/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.qbean;

/**
 * MBean interface.
 * @author apr@cs.com.uy
 * @version $Id: SystemMonitor.java,v 1.2 2005/11/07 20:12:11 marklsalter Exp $
 * @see Logger
 */
public interface SystemMonitorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setSleepTime(long sleepTime) ;

  long getSleepTime() ;

  void setDetailRequired(boolean detail) ;

  boolean getDetailRequired() ;

}
