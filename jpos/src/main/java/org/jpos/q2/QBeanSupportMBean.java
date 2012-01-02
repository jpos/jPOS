/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2012 Alejandro P. Revilla
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

import org.jdom.Element;

import java.net.URL;

/**
 * @author <a href="mailto:taherkordy@dpi2.dpi.net.ir">Alireza Taherkordi</a>
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 */
public interface QBeanSupportMBean extends QBean, QPersist {
    public void setServer (Q2 server);
    public Q2 getServer ();
    public void setPersist (Element e);
    public void setName (String name);
    public String getName ();
    public void setLogger (String name);
    public void setRealm (String realm);
    public String getRealm ();
    public String getLogger ();
    public void shutdownQ2 ();
    public URL[] getLoaderURLS();
    public QClassLoader getLoader();
}

