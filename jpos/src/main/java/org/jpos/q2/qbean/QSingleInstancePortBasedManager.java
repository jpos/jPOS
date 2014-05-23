package org.jpos.q2.qbean;

import java.io.IOException;
import java.net.ServerSocket;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;

public class QSingleInstancePortBasedManager extends QBeanSupport {

    ServerSocket ss;

    int          port;

    /*
     * (non-Javadoc)
     *
     * @see org.jpos.q2.QBeanSupport#initService()
     */
    @Override
    protected void initService() throws Exception {

        try {
            // attempt to bind, if another instance is already bound, an
            // exception will get throwm
            ss = new ServerSocket(port);
        }
        catch (IOException e) {
            getLog().error("An instance of Q2 is already running. Shutting this instance");
            getServer().shutdown();
        }

    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {

        super.setConfiguration(cfg);

        port = cfg.getInt("port", 65000);
    }

}
