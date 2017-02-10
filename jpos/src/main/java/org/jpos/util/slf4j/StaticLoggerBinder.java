package org.jpos.util.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

@SuppressWarnings({"unused", "FinalStaticMethod"})
public class StaticLoggerBinder implements LoggerFactoryBinder
{
    private final ILoggerFactory loggerFactory;

    public static String REQUESTED_API_VERSION = "1.6.99"; // !final
    private static final String loggerFactoryClassStr = JPOSLoggerFactory.class.getName();
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static final StaticLoggerBinder getSingleton()
    {
        return SINGLETON;
    }

    private StaticLoggerBinder()
    {
        loggerFactory = new JPOSLoggerFactory();
    }

    public ILoggerFactory getLoggerFactory()
    {
        return loggerFactory;
    }

    public String getLoggerFactoryClassStr()
    {
        return loggerFactoryClassStr;
    }
}
