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

package org.jpos.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ContextTestCase {
    @Test
    public void testExternalizable () throws Exception {
        Context ctx = new Context();
        ctx.put ("volatile", "the quick brown fox");
        ctx.put ("persistent", "jumped over the lazy dog", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream (baos);
        os.writeObject (ctx);

        ByteArrayInputStream bain = new ByteArrayInputStream (baos.toByteArray());
        ObjectInputStream is = new ObjectInputStream (bain);
        Context ctx1 = (Context) is.readObject();

        // ctx.dump (System.out, "ctx> ");
        // ctx1.dump (System.out, "ctx1>");

        assertNull (ctx1.getString ("volatile"));
        assertNotNull (ctx1.getString ("persistent"));
        assertEquals ("jumped over the lazy dog", ctx1.getString ("persistent"));
    }
}

