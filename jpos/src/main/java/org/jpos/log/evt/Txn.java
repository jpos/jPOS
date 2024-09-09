package org.jpos.log.evt;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.jpos.log.AuditLogEvent;

@JacksonXmlRootElement(localName = "txn")
public record Txn(
    @JacksonXmlProperty(isAttribute = true) String name,
    @JacksonXmlProperty(isAttribute = true) long id) implements AuditLogEvent {
}
