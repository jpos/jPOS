package org.jpos.q2.qbean;

import org.jpos.q2.QBeanSupportMBean;

public interface ChannelAdaptorMBean extends QBeanSupportMBean {
    public void setReconnectDelay (long delay);
    public long getReconnectDelay ();
    public void setInQueue (String in);
    public String getInQueue ();
    public void setOutQueue (String out);
    public String getOutQueue ();
    public void setHost (String host);
    public String getHost ();
    public void setPort (int port);
    public int getPort ();
}
