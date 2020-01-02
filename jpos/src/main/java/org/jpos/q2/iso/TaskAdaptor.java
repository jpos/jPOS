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

package org.jpos.q2.iso;

import org.jdom2.Element;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.QFactory;
import org.jpos.util.Destroyable;
import org.jpos.util.NameRegistrar;

/**
 * Task Adaptor
 *
 * @author Alejandro Revilla
 * @version $Revision$ $Date$
 */
public class TaskAdaptor extends QBeanSupport {
    Object task;

    public TaskAdaptor () {
        super ();
    }

    protected void initService () throws Exception {
        QFactory factory = getServer().getFactory();
        Element e = getPersist ();
        task = factory.newInstance (e.getChildTextTrim ("class"));
        factory.setLogger (task, e);
    }
    protected void startService () throws Exception {
        getServer().getFactory().setConfiguration(task, getPersist());
        NameRegistrar.register (getName (), task);
        if (task instanceof Runnable) {
            new Thread ((Runnable) task).start ();
        }
    }
    protected void stopService () throws Exception {
        NameRegistrar.unregister (getName ());
        if (task instanceof Destroyable)
            ((Destroyable)task).destroy ();
    }
    public Object getObject () {
        return task;
    }
}

