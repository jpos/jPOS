package org.jpos.util.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JPOSLoggerFactory implements ILoggerFactory
{
    private ConcurrentMap<String, Logger> loggerMap=new ConcurrentHashMap<>();

    public Logger getLogger(String name)
    {
        Logger simpleLogger = loggerMap.get(name);
        if (simpleLogger != null)
        {
            return simpleLogger;
        }
        else
        {
            Logger newInstance = new JPOSLogger(name);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
