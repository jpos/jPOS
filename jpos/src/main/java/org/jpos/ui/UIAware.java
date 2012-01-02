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

package org.jpos.ui;

import org.jdom.Element;

/**
 * @since 1.4.7
 * @author Alejandro Revilla
 * <p>
 * An action listener may optional implement this interface in order
 * to have the UI framework push a reference to the UI instance as well
 * as the action's configuration element.
 * </p>
 */
public interface UIAware {
    /**
     * @param ui reference
     * @param config action config block
     */
    public void setUI (UI ui, Element config);
}

