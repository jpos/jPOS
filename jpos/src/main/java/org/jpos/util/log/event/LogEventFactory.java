package org.jpos.util.log.event;

import static org.jpos.util.log.format.JSON.JSON_LABEL;

/**
 * Created by erlangga on 2019-10-12.
 */
public class LogEventFactory {
    public static BaseLogEvent getLogEvent(String format){
        if(JSON_LABEL.equals(format)){
            return new JSONLogEvent();
        }
        return new XMLLogEvent();
    }
}
