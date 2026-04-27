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

package org.jpos.iso.validator;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;

/**
 * This type of exception is raised while validating jPOS ISOComponents.
 * Contains an error component instance referencing to the error.
 * <p>Title: jPOS</p>
 * <p>Description: Java Framework for Financial Systems</p>
 * <p>Copyright: Copyright (c) 2000 jPOS.org.  All rights reserved.</p>
 * <p>Company: www.jPOS.org</p>
 * @author Jose Eduardo Leon
 * @version 1.0
 */
public class ISOVException extends ISOException {

    private static final long serialVersionUID = 8609716526640071611L;
    /**
     * Constructs a validator exception with the given message.
     *
     * @param Description failure description
     */
    public ISOVException( String Description ) {
        super( Description );
    }

    /**
     * Constructs a validator exception with the given message and the offending error component.
     *
     * @param Description failure description
     * @param errComponent the component that produced the error
     */
    public ISOVException( String Description, ISOComponent errComponent ) {
        super( Description );
        this.errComponent = errComponent;
    }

    /**
     * Returns the component that produced the validation error.
     *
     * @return the offending {@link ISOComponent}, or {@code null} if not set
     */
    public ISOComponent getErrComponent(){
        return this.errComponent;
    }

    /**
     * Indicates whether this exception has already been handled by a {@code catch} clause.
     *
     * @return the current treated flag
     */
    public boolean treated() {
        return treated;
    }

    /**
     * Replaces the offending component associated with this exception.
     *
     * @param c the new error component
     */
    public void setErrComponent( ISOComponent c ){
        this.errComponent = c;
    }

    /**
     * Marks this exception as treated (or untreated) by a containing {@code catch} clause.
     *
     * @param Treated new treated flag
     */
    public void setTreated( boolean Treated ){
        treated = Treated;
    }

    /** flag indicating if the exception was catched in any
     * try/catch clause. It is used to determine if it is
     * necessary the replacement of the component by the
     * iso-error-component in the exception instance**/
    protected boolean treated = false;
    /** The component flagged by this validation error. */
    protected ISOComponent errComponent;
}