/*
 * Copyright (c) 2004 jPOS.org
 *
 * See terms of license at http://jpos.org/license.html
 *
 */
package org.jpos.q2.spring;

import org.jpos.q2.QBeanSupport;
import org.jpos.q2.Q2ConfigurationException;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.jpos.util.NameRegistrar;

/**
 * @author Anthony Schexnaildre
 * @version $Revision$ $Date$
 * @jmx:mbean description="Spring QBean" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Spring extends QBeanSupport implements SpringMBean {
    ApplicationContext context;
    String config;

    public void initService () throws Q2ConfigurationException {
        if (config == null)
            throw new Q2ConfigurationException ("config property not specified");
        context = new FileSystemXmlApplicationContext( config.split(",") );
    }

    public void startService () {
        NameRegistrar.register( getName(), this );
    }

    public void stopService () {
        NameRegistrar.unregister( getName() );
    }

    /**
     * Returns the Spring ApplicationContext
     */
    public ApplicationContext getContext () {
	return context;
    }

    /**
     * @jmx:managed-attribute description="Configuration Files"
     */
    public void setConfig (String config) {
        this.config = config;
    }

    /**
     * @jmx:managed-attribute description="Configuration Files"
     */
    public String getConfig () {
        return config;
    }
}

