/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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

package org.jpos.log.render.txt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jpos.log.LogRenderer;
import org.jpos.log.evt.SysInfo;

import java.io.PrintStream;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SysInfoTxtLogRenderer implements LogRenderer<SysInfo> {
    public Class<?> clazz() {
        return SysInfo.class;
    }
    public Type type() {
        return Type.TXT;
    }

    @Override
    public void render(SysInfo info, PrintStream ps, String indent) {
        final String pre = (indent == null) ? "" : indent;
        /*
         * 1) Collect all record components in declaration order
         *    together with the effective (JSON) property name
         *    and the value obtained from the accessor.
         */
        Map<String, Object> data = new LinkedHashMap<>();
        int maxKeyLen = 0;

        for (RecordComponent rc : SysInfo.class.getRecordComponents()) {
            String key = effectiveName(rc);
            Object val;
            try {
                val = rc.getAccessor().invoke(info);
            } catch (Exception e) {
                val = "(error " + e.getMessage() + ")";
            }
            data.put(key, val);
            maxKeyLen = Math.max(maxKeyLen, key.length());
        }

        final int width = maxKeyLen;
        data.forEach((k, v) -> {
            if (v == null) {                         // skip nulls
                return;
            }
            if (v instanceof List<?> list) {
                if (list.isEmpty()) {
                    return;
                }
                ps.printf("%s%-" + width + "s :%n", pre, k);
                list.forEach(o -> ps.println(pre + "  • " + o));
            } else {
                ps.printf("%s%-" + width + "s : %s%n", pre, k, v);
            }
        });
    }

    /** Returns the JSON alias if present, otherwise the plain component name. */
    private static String effectiveName(RecordComponent rc) {
        JsonProperty jp = rc.getAnnotation(JsonProperty.class);
        return (jp != null && !jp.value().isEmpty()) ? jp.value() : rc.getName();
    }
}

