package org.jpos.q2.qbean;

/**
 * MBean interface.
 * @author Alwyn Schoeman
 * @version $Revision: 1476 $ $Date: 2003-04-04 07:43:26 -0300 (Fri, 04 Apr 2003) $
 */
public interface QExecMBean extends org.jpos.q2.QBeanSupportMBean {

  void setStartScript(java.lang.String scriptPath) ;

  java.lang.String getStartScript() ;

  void setShutdownScript(java.lang.String scriptPath) ;

  java.lang.String getShutdownScript() ;

}
