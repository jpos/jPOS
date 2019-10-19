package org.jpos.util.log.format;

import java.io.PrintStream;
import java.util.Date;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSON implements BaseLogFormat {

    public static final String JSON_LABEL = "json";

    @Override
    public void openLogFile(PrintStream p) {
    }

    @Override
    public void closeLogFile(PrintStream p) {
    }

    @Override
    public void logDebug(PrintStream p, String message) {
        p.println("{");
        p.println("\"log\" : {");
        p.println("\"realm\" : \"rotate-log-listener\",");
        p.println("\"at\" : \""+ new Date().toString() +"\",");
        p.println("\"message\" : \""+ message +"\"");
        p.println ("}");
        p.println("}");
    }

    @Override
    public String getType() {
        return JSON_LABEL;
    }
}
