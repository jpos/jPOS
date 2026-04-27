/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
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

import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * JMX management interface for the Q2 thread-pool executor QBean.
 * @author dgrandemange
 * 
 */
public interface QThreadPoolExecutorMBean extends org.jpos.q2.QBeanSupportMBean {
    /**
     * Returns the executor service type.
     * @return executor service type
     */
    String getExecSrvType();

    /**
     * Returns the await-termination delay in milliseconds.
     * @return await termination delay
     */
    int getTerminationTimer();

    /**
     * Returns the approximate number of threads that are actively executing tasks.
     * @return approximate number of threads that are actively executing tasks
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    int getActiveCount() throws NotFoundException;

    /**
     * Returns the approximate total number of tasks that have completed execution.
     * @return the approximate total number of tasks that have completed
     *         execution.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    long getCompletedTaskCount() throws NotFoundException;

    /**
     * Returns the core number of threads.
     * @return returns the core number of threads.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    int getCorePoolSize() throws NotFoundException;

    /**
     * Returns the thread keep-alive time in milliseconds.
     * @return the thread keep-alive time, which is the amount of time (in
     *         milliseconds) which threads in excess of the core pool size may
     *         remain idle before being terminated
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    long getKeepAliveTimeMS() throws NotFoundException;

    /**
     * Returns the largest number of threads that have ever simultaneously been in the pool.
     * @return the largest number of threads that have ever simultaneously been
     *         in the pool.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    int getLargestPoolSize() throws NotFoundException;

    /**
     * Returns the maximum allowed number of threads.
     * @return the maximum allowed number of threads.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    int getMaximumPoolSize() throws NotFoundException;

    /**
     * Returns the current number of threads in the pool.
     * @return the current number of threads in the pool.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    int getPoolSize() throws NotFoundException;

    /**
     * Returns the approximate total number of tasks that have been scheduled for execution.
     * @return the approximate total number of tasks that have been scheduled
     *         for execution
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    long getTaskCount() throws NotFoundException;

    /**
     * Returns whether this executor has been shut down.
     * @return true if this executor has been shut down.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    boolean isShutdown() throws NotFoundException;

    /**
     * Returns whether all tasks have completed following shut down.
     * @return true if all tasks have completed following shut down
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    boolean isTerminated() throws NotFoundException;

    /**
     * Returns whether this executor is in the process of terminating after shutdown.
     * @return true if this executor is in the process of terminating after
     *         shutdown or shutdownNow but has not completely terminated.
     * @throws NotFoundException if the executor cannot be found in the name registrar
     */
    boolean isTerminating() throws NotFoundException;
}
