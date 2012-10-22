package org.jpos.q2.qbean;

import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * @author dgrandemange
 * 
 */
public interface QThreadPoolExecutorMBean extends org.jpos.q2.QBeanSupportMBean {
    /**
     * @return executor service type
     */
    String getExecSrvType();

    /**
     * @return await termination delay
     */
    int getTerminationTimer();

    /**
     * @return approximate number of threads that are actively executing tasks
     */
    int getActiveCount() throws NotFoundException;

    /**
     * @return the approximate total number of tasks that have completed
     *         execution.
     */
    long getCompletedTaskCount() throws NotFoundException;

    /**
     * @return returns the core number of threads.
     */
    int getCorePoolSize() throws NotFoundException;

    /**
     * @return the thread keep-alive time, which is the amount of time (in
     *         milliseconds) which threads in excess of the core pool size may
     *         remain idle before being terminated
     */
    long getKeepAliveTimeMS() throws NotFoundException;

    /**
     * @return the largest number of threads that have ever simultaneously been
     *         in the pool.
     */
    int getLargestPoolSize() throws NotFoundException;

    /**
     * @return the maximum allowed number of threads.
     */
    int getMaximumPoolSize() throws NotFoundException;

    /**
     * @return the current number of threads in the pool.
     */
    int getPoolSize() throws NotFoundException;

    /**
     * @return the approximate total number of tasks that have been scheduled
     *         for execution
     */
    long getTaskCount() throws NotFoundException;

    /**
     * @return true if this executor has been shut down.
     * @throws NotFoundException
     */
    boolean isShutdown() throws NotFoundException;

    /**
     * @return true if all tasks have completed following shut down
     * @throws NotFoundException
     */
    boolean isTerminated() throws NotFoundException;

    /**
     * @return true if this executor is in the process of terminating after
     *         shutdown or shutdownNow but has not completely terminated.
     */
    boolean isTerminating() throws NotFoundException;
}
