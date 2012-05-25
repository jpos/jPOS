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

package  org.jpos.util;

import org.jpos.iso.ISOUtil;

import java.io.PrintStream;

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
            this.msgContent = msgContent;
        }

        public SimpleMsg (String tagName, String msgName, byte[] msgContent) {
            this(tagName, msgName, ISOUtil.hexString(msgContent));
        }

        public SimpleMsg (String tagName, String msgName, boolean msgContent) {
            this(tagName, msgName,  Boolean.valueOf(msgContent));
        }

        public SimpleMsg (String tagName, String msgName, short msgContent) {
            this(tagName, msgName,  Short.valueOf(msgContent));
        }

        public SimpleMsg (String tagName, String msgName, int msgContent) {
            this(tagName, msgName,  Integer.valueOf(msgContent));
        }

        public SimpleMsg (String tagName, String msgName, long msgContent) {
            this(tagName, msgName,  Long.valueOf(msgContent));
        }

        /**
         * dumps message
         * @param p a PrintStream usually supplied by Logger
         * @param indent indention string, usually suppiled by Logger
         * @see org.jpos.util.Loggeable
         */
        public void dump (PrintStream p, String indent) {
            String inner = indent + "  ";
            p.print(indent + "<" + tagName);
            p.print(" name=\"" + msgName + "\"");
            p.println(">");
            if (msgContent instanceof SimpleMsg[]) {
                // dump sub messages
                for (int i = 0; i < ((SimpleMsg[])msgContent).length; i++)
                    ((SimpleMsg[])msgContent)[i].dump(p, inner);
            }
            else if (msgContent instanceof Loggeable)
                ((Loggeable)msgContent).dump(p, inner);
            else
                p.println(inner + msgContent.toString());
            p.println(indent + "</" + tagName + ">");
        }
    }