/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2.qbean;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A qbean dedicated to thread pool executor creation and registration by Q2
 * NameRegistrar registry<br>
 * 
 * @author dgrandemange
 */
public class QThreadPoolExecutor extends QBeanSupport implements
        QThreadPoolExecutorMBean {

    public static final String THREAD_POOL_EXECUTOR__QBEAN_PREFIX = "thread.pool.executor.";

    public static final String XML_CONFIG_ATTR__EXEC_SRV_TYPE = "type";

    public static final String XML_CONFIG_ATTR__EXEC_SRV_COREPOOLSIZE = "corePoolSize";

    public static final String XML_CONFIG_ATTR__EXEC_SRV_TERMINATION_TIMER = "terminationTimer";

    public static final int DEFAULT_TERMINATION_TIMER = 15;

    private String execSrvType;

    private int initialCorePoolSize;

    private int terminationTimer = DEFAULT_TERMINATION_TIMER;

    /**
     * Handle specific config elements
     * 
     * type := "fixed" | "scheduled" | "cached" corePoolSize := integer
     * (required for "fixed" and "scheduled" kinds, optional for "cached" kind)
     * 
     */
    @Override
    protected void initService() throws Exception {
        Element rootElt = this.getPersist();

        Attribute execSrvTypeAttr = getAttribute(rootElt,
                XML_CONFIG_ATTR__EXEC_SRV_TYPE, true,
                "(thread pool executor type among {fixed|cached|scheduled|single})");
        execSrvType = execSrvTypeAttr.getValue().trim();

        if ("fixed".equals(execSrvType)) {
            Attribute corePoolSizeAttr = getAttribute(rootElt,
                    XML_CONFIG_ATTR__EXEC_SRV_COREPOOLSIZE, true,
                    "(number of threads in the pool)");
            initialCorePoolSize = corePoolSizeAttr.getIntValue();

        } else if ("cached".equals(execSrvType)) {
            Attribute corePoolSizeAttr = getAttribute(rootElt,
                    XML_CONFIG_ATTR__EXEC_SRV_COREPOOLSIZE, false,
                    "(number of threads in the pool)");
            if (null != corePoolSizeAttr) {
                initialCorePoolSize = corePoolSizeAttr.getIntValue();
            }

        } else if ("scheduled".equals(execSrvType)) {
            Attribute corePoolSizeAttr = getAttribute(rootElt,
                    XML_CONFIG_ATTR__EXEC_SRV_COREPOOLSIZE, true,
                    "(number of threads in the pool)");
            initialCorePoolSize = corePoolSizeAttr.getIntValue();

        } else {
            throw new ConfigurationException(
                    "Invalid thread pool executor type '%s' (valid types={fixed|cached|scheduled} )");
        }

        Attribute terminationTimerAttr = getAttribute(rootElt,
                XML_CONFIG_ATTR__EXEC_SRV_TERMINATION_TIMER, false,
                "(termination timer in seconds)");
        if (null != terminationTimerAttr) {
            terminationTimer = terminationTimerAttr.getIntValue();
        }

    }

    @Override
    protected void startService() throws Exception {
        ExecutorService execSrv = null;

        try {
            if ("fixed".equals(execSrvType)) {
                execSrv = Executors.newFixedThreadPool(initialCorePoolSize);
            } else if ("cached".equals(execSrvType)) {
                execSrv = Executors.newCachedThreadPool();
                if (initialCorePoolSize != 0) {
                    ((ThreadPoolExecutor) execSrv)
                            .setCorePoolSize(initialCorePoolSize);
                }
            } else if ("scheduled".equals(execSrvType)) {
                execSrv = Executors.newScheduledThreadPool(initialCorePoolSize);
            }

            if (null != execSrv) {
                NameRegistrar.register(getRegistrationName(), execSrv);
            } else {
                throw new Exception(
                        "Unable to start service : thread pool executor instance is null");
            }
        } catch (Exception e) {
            if (null != execSrv) {
                try {
                    execSrv.shutdownNow();
                } catch (Exception ee) {
                    getLog().warn(ee);
                }
            }
            throw e;
        }
    }

    @Override
    protected void stopService() throws Exception {
        ThreadPoolExecutor execSrv = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);

        if (null != execSrv) {
            execSrv.shutdownNow();

            boolean awaitTermination = execSrv.awaitTermination(
                    terminationTimer, TimeUnit.SECONDS);

            if (awaitTermination) {
                NameRegistrar.unregister(getRegistrationName());
            } else {
                throw new Exception(
                        String.format(
                                "Unable to shutdown thread pool executor : executor termination delay (%d seconds) has expired",
                                terminationTimer));
            }
        } else {
            throw new Exception(
                    String.format(
                            "Unable to stop thread pool executor : no executor '%s' found registered under name '%s'",
                            getName(), getRegistrationName()));
        }
    }

    protected String getRegistrationName() {
        return THREAD_POOL_EXECUTOR__QBEAN_PREFIX + getName();
    }

    /**
     * @param elt
     * @param attrName
     * @param mandatory
     * @param errDesc
     * @throws ConfigurationException
     */
    protected Attribute getAttribute(Element elt, String attrName,
            boolean mandatory, String errDesc) throws ConfigurationException {
        Attribute attr = elt.getAttribute(attrName);

        if (null == attr || "".equals(attr.getValue().trim())) {
            if (mandatory) {
                throw new ConfigurationException(String.format(
                        "'%s' attribute has not been found or is empty %s",
                        XML_CONFIG_ATTR__EXEC_SRV_TYPE, errDesc));
            } else {
                return null;
            }
        } else {
            return attr;
        }
    }

    /**
     * Retrieves a thread pool executor from NameRegistrar given its name
     * 
     * @param name
     * @throws NotFoundException
     */
    public static ThreadPoolExecutor getThreadPoolExecutor(java.lang.String name)
            throws NotFoundException {
        ThreadPoolExecutor res = null;
        Object object = NameRegistrar.get(THREAD_POOL_EXECUTOR__QBEAN_PREFIX
                + name);
        if (object instanceof ThreadPoolExecutor) {
            res = (ThreadPoolExecutor) object;
        } else {
            throw new NotFoundException(name);
        }

        return res;
    }

    /**
     * Retrieves a thread pool executor from NameRegistrar given its name, and
     * its expected class
     * 
     * @param name
     * @param clazz
     * @throws NotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T extends ThreadPoolExecutor> T getThreadPoolExecutor(
            java.lang.String name, Class<T> clazz) throws NotFoundException {
        T res = null;

        Object object = NameRegistrar.get(THREAD_POOL_EXECUTOR__QBEAN_PREFIX
                + name);

        if (clazz.isAssignableFrom(object.getClass())) {
            res = (T) object;
        } else {
            throw new NotFoundException(name);
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getExecSrvType()
     */
    public String getExecSrvType() {
        return execSrvType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getTerminationTimer()
     */
    public int getTerminationTimer() {
        return terminationTimer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getActiveCount()
     */
    public int getActiveCount() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getActiveCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getCompletedTaskCount()
     */
    public long getCompletedTaskCount() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getCompletedTaskCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getCorePoolSize()
     */
    public int getCorePoolSize() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getCorePoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getKeepAliveTimeMS()
     */
    public long getKeepAliveTimeMS() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getKeepAliveTime(TimeUnit.MILLISECONDS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getLargestPoolSize()
     */
    public int getLargestPoolSize() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getLargestPoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getMaximumPoolSize()
     */
    public int getMaximumPoolSize() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getMaximumPoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getPoolSize()
     */
    public int getPoolSize() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getPoolSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#getTaskCount()
     */
    public long getTaskCount() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.getTaskCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#isShutdown()
     */
    public boolean isShutdown() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.isShutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#isTerminated()
     */
    public boolean isTerminated() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.isTerminated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jpos.q2.qbean.QExecutorServiceMBean#isTerminating()
     */
    public boolean isTerminating() throws NotFoundException {
        ThreadPoolExecutor executorService = getThreadPoolExecutor(getName(),
                ThreadPoolExecutor.class);
        return executorService.isTerminating();
    }

    public int getInitialCorePoolSize() {
        return initialCorePoolSize;
    }

    protected void setExecSrvType(String execSrvType) {
        this.execSrvType = execSrvType;
    }

    protected void setInitialCorePoolSize(int initialCorePoolSize) {
        this.initialCorePoolSize = initialCorePoolSize;
    }

    protected void setTerminationTimer(int terminationTimer) {
        this.terminationTimer = terminationTimer;
    }

}
