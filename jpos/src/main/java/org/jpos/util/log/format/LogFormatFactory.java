package org.jpos.util.log.format;

import static org.jpos.util.log.format.JSON.JSON_LABEL;

/**
 * Created by erlangga on 2019-10-12.
 */
public class LogFormatFactory {
    public static BaseLogFormat getLogFormat(String format){
        if(JSON_LABEL.equals(format)){
            return new JSON();
        }
        return new XML();
    }
}
