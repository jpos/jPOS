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
import org.jpos.iso.ISOMsg;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
    
/** 
 * 
 * This class can be used by the Log4J framework to render ISOMsg objects.
 * 
 * <pre>
 * Add the following section into the Log4J configuration
 * file to use this renderer
 *
 * &lt;renderer
 *     renderedClass="org.jpos.iso.ISOMsg"
 *     renderingClass="org.jpos.log4j.ISOMsgRenderer"&gt;
 * &lt;/renderer&gt;
 * </pre>
 *
 * @see ObjectRenderer
 * @see ISOMsg
 * @author Eoin FLood
 */
public class ISOMsgRenderer implements ObjectRenderer
{
    /**
     * Convert an ISOMsg to an XML representation
     * that is suitable for logging.
     */
    public String doRender (Object o)
    {
        if (o instanceof ISOMsg)
        {
            ByteArrayOutputStream str = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream (str);
            ISOMsg msg = (ISOMsg) o;
            msg.dump (ps, "");

            return "\n" + str.toString();
        }
        else
            return "ERROR: ISOMsgRenderer can only render ISOMsg objects";
    }
}
