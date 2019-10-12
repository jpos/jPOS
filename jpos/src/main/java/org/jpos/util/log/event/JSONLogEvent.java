package org.jpos.util.log.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSONLogEvent implements BaseLogEvent {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public String dumpHeader(PrintStream p, String indent, String realm, Instant dumpedAt, Instant createdAt, boolean noArmor) {
        if (noArmor) {
            p.println("");
        } else {
            if (dumpedAt == null)
                dumpedAt = Instant.now();

            StringBuilder sb = new StringBuilder(indent);
            sb.append ("\"log\":{\n");
            sb.append ("\"realm\":"+ "\""+realm+"\",\n");
            sb.append("\"at\":");
            sb.append("\""+LocalDateTime.ofInstant(dumpedAt, ZoneId.systemDefault())+"\"");

            long elapsed = Duration.between(createdAt, dumpedAt).toMillis();
            if (elapsed > 0) {
                sb.append(",\n");
                sb.append ("\"lifespan\":\"");
                sb.append (elapsed);
                sb.append ("ms\"");
            }

            sb.append ("\n}");
            p.print(sb.toString());
        }
        return indent + "  ";
    }

    @Override
    public void dumpTrailer(PrintStream p, String indent, boolean noArmor) {
        if (!noArmor)
            p.println (indent + "}");
    }

    @Override
    public void dump(PrintStream p, String outer, String realm, Instant dumpedAt, Instant createdAt, List<Object> payLoad, boolean noArmor, String tag) {
        try{
            String indent = dumpHeader (p, outer, realm,dumpedAt,createdAt, noArmor);
            p.println(indent);
        }finally {
            dumpTrailer(p,outer,noArmor);
        }

    }
}
