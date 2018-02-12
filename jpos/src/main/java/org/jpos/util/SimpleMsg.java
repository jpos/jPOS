/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2018 jPOS Software SRL
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

package  org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

/**
 * <p>
 * A simple general purpose loggeable message.
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
    public class SimpleMsg
            implements Loggeable {
        String tagName;
        String msgName;
        Object msgContent;

        public SimpleMsg (String tagName, String msgName, Object msgContent) {
            this.tagName = tagName;
            this.msgName = msgName;
            if(msgContent instanceof byte[])
              this.msgContent = ISOUtil.hexString((byte[])msgContent);
            else
              this.msgContent = msgContent;
        }

        public SimpleMsg (String tagName, Object msgContent) {
          this(tagName, null, msgContent);
        }

        /**
         * dumps message
         * @param p a PrintStream usually supplied by Logger
         * @param indent indention string, usually suppiled by Logger
         * @see org.jpos.util.Loggeable
         */
        @Override
        public void dump (PrintStream p, String indent) {
            String inner = indent + "  ";
            p.print(indent + "<" + tagName);
            if (msgName != null)
                p.print(" name=\"" + msgName + "\"");

            Collection cl = null;
            if (msgContent instanceof Object[])
                cl = Arrays.asList((Object[]) msgContent);
            else if (msgContent instanceof Collection)
                cl = (Collection) msgContent;
            else if (msgContent instanceof Loggeable)
                cl = Arrays.asList(msgContent);
            else if (msgName != null && msgContent == null){
                p.println("/>");
                return;
            } else if (msgName != null)
                cl = Arrays.asList(msgContent);
            else if (msgContent != null)
                p.print(">" + msgContent);
            else {
                p.println("/>");
                return;
            }

            if (cl != null) {
                p.println(">");
                for (Object o : cl) {
                    if (o instanceof Loggeable)
                        ((Loggeable) o).dump(p, inner);
                    else
                        p.println(inner + o);
                }
                p.print(indent);
            }

            p.println("</" + tagName + ">");
        }
    }