package org.jpos.tlv.packager;


import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * @author Vishnu Pillai
 */
public class PackagerErrorLogger implements PackagerErrorHandler, Configurable, LogSource {

    private Logger logger = Logger.getLogger("PACKAGER_ERROR_LOGGER");
    private String realm;

    @Override
    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        String loggerName = cfg.get("packager-error-logger");
        logger = Logger.getLogger(loggerName);
        realm = cfg.get("realm", "packager-error");
    }

    @Override
    public void handlePackError(ISOComponent m, ISOException e) {
        StringBuilder sb = new StringBuilder("Error packing message:\n");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        m.dump(ps, "");
        sb.append(e.toString()).append("\n");
        sb.append("Message dump:\n").append(new String(baos.toByteArray())).append("\n");
        LogEvent logEvent = new LogEvent(this, "pack-error");
        logEvent.addMessage(sb.toString());
        logEvent.addMessage(e);
        Logger.log(logEvent);
    }

    @Override
    public void handleUnpackError(ISOComponent isoComponent, byte[] msg, ISOException e) {
        StringBuilder sb = new StringBuilder("Error unpacking message:\n");
        sb.append(e.toString()).append("\n");
        sb.append("Raw data:\n").append(new String(msg)).append("\n");
        LogEvent logEvent = new LogEvent(this, "unpack-error");
        logEvent.addMessage(sb.toString());
        logEvent.addMessage(e);
        Logger.log(logEvent);
    }
}
