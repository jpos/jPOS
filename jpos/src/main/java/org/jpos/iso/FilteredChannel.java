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

package org.jpos.iso;

import java.util.Collection;

/**
 * Filtered Channel
 * 
 * @author <a href="mailto:apr@cs.com.uy">Alejandro P. Revilla</a>
 * @version $Revision$ $Date$
 * @see ISOChannel
 * @see ServerChannel
 */

public interface FilteredChannel extends ISOChannel {
    /**
     * @param filter incoming filter to add
     */
    public void addIncomingFilter (ISOFilter filter);

    /**
     * @param filter outgoing filter to add
     */
    public void addOutgoingFilter (ISOFilter filter);

    public void addFilter (ISOFilter filter);

    /**
     * @param filter filter to remove (both directions)
     */
    public void removeFilter (ISOFilter filter);

    /**
     * @param filter incoming filter to remove
     */
    public void removeIncomingFilter (ISOFilter filter);

    /**
     * @param filter outgoing filter to remove
     */
    public void removeOutgoingFilter (ISOFilter filter);

   /**
    * @return Collection containing all incoming filters
    */
    public Collection getIncomingFilters();

   /**
    * @return Collection containing all outgoing filters
    */
    public Collection getOutgoingFilters();

   /**
    * @param filters incoming filter set
    */
    public void setIncomingFilters (Collection filters);

   /**
    * @param filters outgoing filter set
    */
    public void setOutgoingFilters (Collection filters);
}

