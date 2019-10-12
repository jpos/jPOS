package org.jpos.util.log.format;

/**
 * Created by erlangga on 2019-10-12.
 */
public class LogFormatFactory {
    public static BaseLogFormat getLogFormat(String format){
        if(format.equals("xml")){
            return new XML();
        }else if(format.equals("json")){

        }
        return null;
    }
}
