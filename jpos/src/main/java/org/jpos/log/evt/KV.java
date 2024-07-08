package org.jpos.log.evt;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

@JacksonXmlRootElement(localName = "entry")
public record KV(
  @JacksonXmlProperty(isAttribute = true, localName = "key") String key,
  @JacksonXmlText String value) { }
