package org.jpos.log.evt;

import org.jpos.log.AuditLogEvent;

public record SessionEnd(int connections, int permits, String info) implements AuditLogEvent { }
