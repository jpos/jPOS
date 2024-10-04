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

package org.jpos.log.evt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Record representing the output of a process, encapsulating the process name,
 * standard output, and optionally standard error.
 *
 * <p>This record makes use of Jackson annotations to control the serialization
 * and deserialization process. The {@code name} field is serialized as an
 * attribute with the local name "name". The {@code stdout} field is serialized
 * as XML text. The {@code stderr} field is included in the JSON output only if
 * it is non-null.
 *
 * @param name   the name of the process, serialized as an XML attribute with the local name "name"
 * @param stdout the standard output of the process, serialized as XML text
 * @param stderr the standard error of the process, included in JSON output only if non-null
 */
public record ProcessOutput(
  @JacksonXmlProperty(isAttribute = true, localName = "name") String name,
  @JacksonXmlText String stdout,
  @JsonInclude(JsonInclude.Include.NON_NULL) String stderr)
{ }
