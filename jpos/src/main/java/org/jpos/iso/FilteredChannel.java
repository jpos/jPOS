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
     * Adds a filter to the incoming filter chain.
     * @param filter incoming filter to add
     */
    void addIncomingFilter(ISOFilter filter);

    /**
     * Adds a filter to the outgoing filter chain.
     * @param filter outgoing filter to add
     */
    void addOutgoingFilter(ISOFilter filter);

    /** Adds a filter to both incoming and outgoing chains.
     * @param filter filter to add
     */
    void addFilter(ISOFilter filter);

    /**
     * Removes a filter from both incoming and outgoing chains.
     * @param filter filter to remove (both directions)
     */
    void removeFilter(ISOFilter filter);

    /**
     * Removes a filter from the incoming filter chain.
     * @param filter incoming filter to remove
     */
    void removeIncomingFilter(ISOFilter filter);

    /**
     * Removes a filter from the outgoing filter chain.
     * @param filter outgoing filter to remove
     */
    void removeOutgoingFilter(ISOFilter filter);

   /**
    * Returns all filters in the incoming filter chain.
    * @return Collection containing all incoming filters
    */
   Collection getIncomingFilters();

   /**
    * Returns all filters in the outgoing filter chain.
    * @return Collection containing all outgoing filters
    */
   Collection getOutgoingFilters();

   /**
    * Replaces the entire incoming filter chain with the given collection.
    * @param filters incoming filter set
    */
   void setIncomingFilters(Collection filters);

   /**
    * Replaces the entire outgoing filter chain with the given collection.
    * @param filters outgoing filter set
    */
   void setOutgoingFilters(Collection filters);
}

