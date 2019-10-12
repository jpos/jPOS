package org.jpos.util.log.format;

import java.io.PrintStream;

/**
 * Created by erlangga on 2019-10-12.
 */
public class JSON implements BaseLogFormat {

    public static final String JSON_LABEL = "json";

    @Override
    public void openLogFile(PrintStream p) {
        p.println ("{");
    }

    @Override
    public void closeLogFile(PrintStream p) {
        p.println ("}");
    }

    @Override
    public void logDebug(PrintStream p, String message) {
        // TODO
    }

    @Override
    public String getType() {
        return JSON_LABEL;
    }
}
