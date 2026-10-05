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

package org.jpos.iso;

import org.jpos.util.FSDMsg;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FSDISOMsgExternalizationTest {
    @Test
    void readExternalAcceptsUnknownVersionAndValidStringMap() throws Exception {
        FSDISOMsg message = new FSDISOMsg();
        try (ObjectInputStream in = externalizedInput(127, "path-", "schema", Map.of("11", "123456"))) {
            message.readExternal(in);
        }

        assertEquals("path-", message.getFSDMsg().getBasePath());
        assertEquals("schema", message.getFSDMsg().getBaseSchema());
        assertEquals("123456", message.getString("11"));
    }

    @Test
    void readExternalRejectsNonMapWithoutChangingState() throws Exception {
        FSDMsg original = originalFsd();
        FSDISOMsg message = new FSDISOMsg(original);
        try (ObjectInputStream in = externalizedInput(0, "new-", "new", "not a map")) {
            InvalidObjectException exception = assertThrows(InvalidObjectException.class,
              () -> message.readExternal(in));
            assertEquals("Invalid field map in serialized FSDISOMsg", exception.getMessage());
        }

        assertOriginalState(message, original);
    }

    @Test
    void readExternalRejectsNonStringEntryWithoutChangingState() throws Exception {
        FSDMsg original = originalFsd();
        FSDISOMsg message = new FSDISOMsg(original);
        Map<Object,Object> fields = new LinkedHashMap<>();
        fields.put("valid", "value");
        fields.put("invalid", 1);
        try (ObjectInputStream in = externalizedInput(0, "new-", "new", fields)) {
            InvalidObjectException exception = assertThrows(InvalidObjectException.class,
              () -> message.readExternal(in));
            assertEquals("Invalid field entry in serialized FSDISOMsg", exception.getMessage());
        }

        assertOriginalState(message, original);
    }

    @Test
    void readExternalRejectsExcessiveMapWithoutChangingState() throws Exception {
        FSDMsg original = originalFsd();
        FSDISOMsg message = new FSDISOMsg(original);
        Map<String,String> fields = new LinkedHashMap<>();
        for (int i = 0; i < 10_001; i++)
            fields.put(Integer.toString(i), "value");
        try (ObjectInputStream in = externalizedInput(0, "new-", "new", fields)) {
            InvalidObjectException exception = assertThrows(InvalidObjectException.class,
              () -> message.readExternal(in));
            assertEquals("Too many entries in serialized FSDISOMsg", exception.getMessage());
        }

        assertOriginalState(message, original);
    }

    private FSDMsg originalFsd() {
        FSDMsg fsd = new FSDMsg("original-", "original");
        fsd.set("field", "old value");
        return fsd;
    }

    private void assertOriginalState(FSDISOMsg message, FSDMsg original) {
        assertSame(original, message.getFSDMsg());
        assertEquals("old value", message.getString("field"));
    }

    private ObjectInputStream externalizedInput(int version, String basePath, String baseSchema, Object fields)
      throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeByte(version);
            out.writeUTF(basePath);
            out.writeUTF(baseSchema);
            out.writeObject(fields);
        }
        return new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
    }
}
