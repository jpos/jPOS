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

package org.jpos.transaction;


public interface TransactionConstants {
    public static final int ABORTED  = 0;
    public static final int PREPARED = 1;
    public static final int RETRY    = 2;
    public static final int PAUSE    = 4;

    /**
     * This participant does not join the transaction
     */
    public static final int NO_JOIN  = 0x40;

    /**
     * Context has not been modified (no need to persist a snapshot)
     */
    public static final int READONLY = 0x80;
}

