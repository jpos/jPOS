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
    * @filters incoming filter set
    */
    public void setIncomingFilters (Collection filters);

   /**
    * @filters outgoing filter set
    */
    public void setOutgoingFilters (Collection filters);
}

