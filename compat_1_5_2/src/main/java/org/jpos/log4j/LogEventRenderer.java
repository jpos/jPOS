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

package org.jpos.log4j;

import org.apache.log4j.or.ObjectRenderer;
import org.jpos.util.LogEvent;
    
/** 
 * 
 * This class can be used by the Log4J framework to render LogEvents objects.
 * 
 * <pre>
 * Add the following section into the Log4J configuration
 * file to use this renderer
 *
 * &lt;renderer
 *     renderedClass="org.jpos.util.LogEvent"
 *     renderingClass="org.jpos.log4j.LogEventRenderer"&gt;
 * &lt;/renderer&gt;
 * </pre>
 *
 * @see ObjectRenderer
 * @see LogEvent
 * @author Alejandro Revilla (based on Eoin's work)
 */
public class LogEventRenderer implements ObjectRenderer
{
    /**
     * Convert an ISOMsg to an XML representation
     * that is suitable for logging.
     */
    public String doRender (Object o)
    {
        if (o instanceof LogEvent)
        {
            return "\n" + o.toString();
        }
        else
            return "ERROR: LogEventRenderer can only render LogEvent objects";
    }
}
