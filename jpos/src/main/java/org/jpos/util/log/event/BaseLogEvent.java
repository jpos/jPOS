package org.jpos.util.log.event;

import java.io.PrintStream;
import java.time.Instant;
import java.util.List;

/**
 * Created by erlangga on 2019-10-12.
 */
public interface BaseLogEvent {
    String dumpHeader(PrintStream p, String indent, String realm, Instant dumpedAt, Instant createdAt, boolean noArmor);
    void dumpTrailer (PrintStream p, String indent, boolean noArmor);
    void dump (PrintStream p, String outer,String realm, Instant dumpedAt, Instant createdAt, List<Object> payLoad, boolean noArmor, String tag);
    String addMessage(String tagname, String message);
}
