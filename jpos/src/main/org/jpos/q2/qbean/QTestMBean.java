package org.jpos.q2.qbean;

import org.jpos.q2.QBeanSupportMBean;

public interface QTestMBean extends QBeanSupportMBean {
    public void setTickInterval (long tickInterval);
    public long getTickInterval ();
}
