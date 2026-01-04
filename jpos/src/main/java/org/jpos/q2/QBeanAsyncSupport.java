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

package org.jpos.q2;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class QBeanAsyncSupport extends QBeanSupport {

    private long startMaxWait;
    private long stopMaxWait;

    @Override
    protected final void startService() throws Exception {
        ScheduledFuture<?> future = getScheduledThreadPoolExecutor().schedule(() -> {
            try {
                doStart();
            } catch (Exception e) {
                setState(QBean.FAILED);
                log.error(e);
            }
        }, 0, TimeUnit.MILLISECONDS);
        if (startMaxWait > 0) {
            future.get(startMaxWait, TimeUnit.MILLISECONDS);
        }
    }

    protected abstract void doStart() throws Exception;

    @Override
    protected final void stopService() throws Exception {
        ScheduledFuture<?> future = getScheduledThreadPoolExecutor().schedule(() -> {
            try {
                doStop();
            } catch (Exception e) {
                log.error(e);
            }
        }, 0, TimeUnit.MILLISECONDS);
        if (stopMaxWait > 0) {
            future.get(stopMaxWait, TimeUnit.MILLISECONDS);
        }
    }

    protected abstract void doStop() throws Exception;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        startMaxWait = cfg.getLong("startMaxWait", 0L);
        stopMaxWait = cfg.getLong("stopMaxWait", 0L);
    }
}
