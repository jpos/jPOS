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
