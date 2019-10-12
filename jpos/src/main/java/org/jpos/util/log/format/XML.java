package org.jpos.util.log.format;

import java.io.PrintStream;
import java.util.Date;

/**
 * Created by erlangga on 2019-10-12.
 */
public class XML implements BaseLogFormat {

    public static final String XML_LABEL = "xml";

    @Override
    public void openLogFile(PrintStream p) {
        p.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        p.println ("<logger class=\"" + getClass().getName() + "\">");
    }

    @Override
    public void closeLogFile(PrintStream p) {
        p.println ("</logger>");
    }

    @Override
    public void logDebug(PrintStream p, String msg){
        p.println ("<log realm=\"rotate-log-listener\" at=\""+new Date().toString() +"\">");
        p.println ("   "+msg);
        p.println ("</log>");
    }

    @Override
    public String getType() {
        return XML_LABEL;
    }
}
