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

package org.jpos.core;

import org.jpos.tpl.PersistentEngine;

/**
 * @author apr@cs.com.uy
 * @version $Revision$ $Date$
 * @since jPOS 1.1
 *
 * Implements financial institution specific functionality<br>
 * CardAgent may rely on <b><i>j</i>POS</b>'s ISO package
 * for the low level interchange implementation.
 */
public interface CardAgent {
    /**
     * @return agent unique ID
     */
    int getID();

    /**
     * @return Configuration instance
     */
    Configuration getConfiguration();

    /**
     * @param t CardTransaction
     * @return true if agent is able/willing to handle this transaction
     */
    boolean canHandle(CardTransaction t);

    /**
     * Process the transaction
     * @param t previously promoted CardTransaction
     * @return CardTransactionInfo object associated with this transaction
     * @exception CardAgentException
     */
    CardTransactionResponse process(CardTransaction t)
        throws CardAgentException;

    /**
     * @return property prefix used in configuration
     */
    String getPropertyPrefix();

    /**
     * Set PersistentEngine associated with this CardAgent
     * @param engine a PersistentEngine instance
     */
    void setPersistentEngine(PersistentEngine engine);

    /**
     * @return PersistentEngine instance
     */
    PersistentEngine getPersistentEngine();

}
