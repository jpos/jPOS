package org.jpos.log.evt;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jpos.log.AuditLogEvent;

public record LogMessage(@JsonProperty("m") String msg) implements AuditLogEvent {
    @Override
    public String toString() {
        return "LogMessage{" +
          "msg='" + msg + '\'' +
          '}';
    }
}
