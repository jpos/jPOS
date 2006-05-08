/*
 * Generated file - Do not edit!
 */
package org.jpos.q2.iso;

/**
 * MBean interface.
 * @author Alejandro Revilla
 * @version $Revision: 1859 $ $Date: 2003-12-05 23:52:20 -0300 (Fri, 05 Dec 2003) $
 */
public interface DirPollAdaptorMBean extends org.jpos.q2.QBeanSupportMBean {

  void setPath(java.lang.String path) ;

  void setPoolSize(int size) ;

  int getPoolSize() ;

  java.lang.String getPath() ;

  void setPollInterval(long pollInterval) ;

  long getPollInterval() ;

  void setPriorities(java.lang.String priorities) ;

  java.lang.String getPriorities() ;

  void setProcessor(java.lang.String processor) ;

  java.lang.String getProcessor() ;

}
