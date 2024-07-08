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
