package org.jpos.util.log.format;

import java.io.PrintStream;

/**
 * Created by erlangga on 2019-10-12.
 */
public interface BaseLogFormat {
    void openLogFile(PrintStream p);
    void closeLogFile(PrintStream p);
    void logDebug(PrintStream p, String message);
}
