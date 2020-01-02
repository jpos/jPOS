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

import org.jpos.q2.Q2;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.LocalSpace;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

import javax.management.ObjectName;
import java.util.Set;

/**
 * Space Adaptor
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */

public class SpaceAdaptor 
    extends QBeanSupport
    implements SpaceAdaptorMBean
{
    private Space sp = null;
    private String spaceName = null;
    private ObjectName objectName = null;

    public SpaceAdaptor () {
        super ();
    }

    protected void startService () throws Exception {
        if (spaceName == null) 
            sp = SpaceFactory.getSpace ();
        else 
            sp = SpaceFactory.getSpace (spaceName);

        objectName = new ObjectName (Q2.QBEAN_NAME + 
            getName() + ",space=" +
            (spaceName != null ? spaceName : "default")
        );
        getServer().getMBeanServer().registerMBean (sp, objectName);
    }

    protected void stopService () throws Exception {
        getServer().getMBeanServer().unregisterMBean (objectName);
    }

    public synchronized void setSpaceName (String spaceName) {
        this.spaceName = spaceName;
        setAttr (getAttrs (), "spaceName", spaceName);
        setModified (true);
    }

    public String getSpaceName () {
        return spaceName;
    }
    
    public Set getKeys () {
        if (sp instanceof LocalSpace) 
            return ((LocalSpace)sp).getKeySet ();
        return null;
    }
}

