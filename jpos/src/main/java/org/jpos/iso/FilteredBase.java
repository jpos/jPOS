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

package org.jpos.iso;

import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.util.LogEvent;

import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Filtered Channel Base.
 *
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see FilteredChannel
 */

@SuppressWarnings("unchecked")
public abstract class FilteredBase extends Observable
    implements FilteredChannel, Cloneable
{

    protected List<ISOFilter> incomingFilters;

    protected List<ISOFilter> outgoingFilters;

    public FilteredBase () {
        super();
        incomingFilters = new CopyOnWriteArrayList<>();
        outgoingFilters = new CopyOnWriteArrayList<>();
    }

    /**
     * @param filter filter to add
     * @param direction ISOMsg.INCOMING, ISOMsg.OUTGOING, 0 for both
     */
    private void addFilter (ISOFilter filter, int direction) {
        switch (direction) {
            case ISOMsg.INCOMING :
                incomingFilters.add (filter);
                break;
            case ISOMsg.OUTGOING :
                outgoingFilters.add (filter);
                break;
            case 0 :
                incomingFilters.add (filter);
                outgoingFilters.add (filter);
                break;
        }
    }
    /**
     * @param filter incoming filter to add
     */
    @Override
    public void addIncomingFilter (ISOFilter filter) {
        addFilter (filter, ISOMsg.INCOMING);
    }
    /**
     * @param filter outgoing filter to add
     */
    @Override
    public void addOutgoingFilter (ISOFilter filter) {
        addFilter (filter, ISOMsg.OUTGOING);
    }

    /**
     * @param filter filter to add (both directions, incoming/outgoing)
     */
    @Override
    public void addFilter (ISOFilter filter) {
        addFilter (filter, 0);
    }
    /**
     * @param filter filter to remove
     * @param direction ISOMsg.INCOMING, ISOMsg.OUTGOING, 0 for both
     */
    private void removeFilter (ISOFilter filter, int direction) {
        switch (direction) {
            case ISOMsg.INCOMING :
                incomingFilters.remove (filter);
                break;
            case ISOMsg.OUTGOING :
                outgoingFilters.remove (filter);
                break;
            case 0 :
                incomingFilters.remove (filter);
                outgoingFilters.remove (filter);
                break;
        }
    }
    /**
     * @param filter filter to remove (both directions)
     */
    @Override
    public void removeFilter (ISOFilter filter) {
        removeFilter (filter, 0);
    }
    /**
     * @param filter incoming filter to remove
     */
    @Override
    public void removeIncomingFilter (ISOFilter filter) {
        removeFilter (filter, ISOMsg.INCOMING);
    }
    /**
     * @param filter outgoing filter to remove
     */
    @Override
    public void removeOutgoingFilter (ISOFilter filter) {
        removeFilter (filter, ISOMsg.OUTGOING);
    }
    protected ISOMsg applyOutgoingFilters (ISOMsg m, LogEvent evt)
        throws VetoException
    {
        for (ISOFilter f : outgoingFilters) {
            m.setDirection(ISOMsg.OUTGOING);
            f.filter(this, m, evt);
        }

        m.setDirection(ISOMsg.OUTGOING);
        setChanged ();
        notifyObservers (m);
        return m;
    }
    protected ISOMsg applyIncomingFilters (ISOMsg m, LogEvent evt)
        throws VetoException
    {
        for (ISOFilter f : incomingFilters) {
            m.setDirection(ISOMsg.INCOMING);
            f.filter(this, m, evt);
        }

        m.setDirection(ISOMsg.INCOMING);
        setChanged ();
        notifyObservers (m);
        return m;
    }

    @Override
    public Collection getIncomingFilters() {
        return incomingFilters;
    }

    @Override
    public Collection getOutgoingFilters() {
        return outgoingFilters;
    }

    @Override
    public void setIncomingFilters(Collection filters) {
        incomingFilters = new CopyOnWriteArrayList<>(filters);
    }

    @Override
    public void setOutgoingFilters(Collection filters) {
        outgoingFilters = new CopyOnWriteArrayList<>(filters);
    }

    @Override
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError();
      }
    }

}

