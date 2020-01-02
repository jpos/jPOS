/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.util.slf4j;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.util.Log;
import org.jpos.util.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

@SuppressWarnings("WeakerAccess")
public class JPOSLogger extends MarkerIgnoringBase
{
    private Log log;

    private static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
    private static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
    private static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
    private static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
    private static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;

    protected int DEFAULT_LOG_LEVEL = LOG_LEVEL_INFO;

    private static int stringToLevel(String levelStr)
    {
        if ("trace".equalsIgnoreCase(levelStr))
        {
            return LOG_LEVEL_TRACE;
        }
        else if ("debug".equalsIgnoreCase(levelStr))
        {
            return LOG_LEVEL_DEBUG;
        }
        else if ("info".equalsIgnoreCase(levelStr))
        {
            return LOG_LEVEL_INFO;
        }
        else if ("warn".equalsIgnoreCase(levelStr))
        {
            return LOG_LEVEL_WARN;
        }
        else if ("error".equalsIgnoreCase(levelStr))
        {
            return LOG_LEVEL_ERROR;
        }
        return LOG_LEVEL_INFO;
    }

    public JPOSLogger(String name)
    {
        this.log = Log.getLog("Q2", "slf4j::" + name);
    }

    protected boolean isLevelEnabled(int logLevel)
    {
        Logger logger = log.getLogger();
        Configuration cfg = logger.getConfiguration();
        if (cfg == null)
            cfg = new SimpleConfiguration();
        String levelString = cfg.get("slf4j.level", System.getProperty("slf4j.level"));
        int currentLogLevel = levelString != null ? stringToLevel(levelString) : DEFAULT_LOG_LEVEL;
        return (logLevel >= currentLogLevel);
    }

    @Override
    public boolean isTraceEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    @Override
    public void trace(String msg)
    {
        if (isTraceEnabled())
        {
            log.trace(msg);
        }
    }

    @Override
    public void trace(String format, Object arg)
    {
        if (isTraceEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            log.trace(ft.getMessage());
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2)
    {
        if (isTraceEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            log.trace(ft.getMessage());
        }
    }

    @Override
    public void trace(String format, Object... arguments)
    {
        if (isTraceEnabled())
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            log.trace(ft.getMessage());
        }
    }

    @Override
    public void trace(String msg, Throwable t)
    {
        if (isTraceEnabled())
        {
            log.trace(msg, t);
        }
    }

    @Override
    public void debug(String msg)
    {
        if (isDebugEnabled())
        {
            log.debug(msg);
        }
    }

    @Override
    public void debug(String format, Object arg)
    {
        if (isDebugEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            log.debug(ft.getMessage());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2)
    {
        if (isDebugEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            log.debug(ft.getMessage());
        }
    }

    @Override
    public void debug(String format, Object... arguments)
    {
        if (isDebugEnabled())
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            log.debug(ft.getMessage());
        }
    }

    @Override
    public void debug(String msg, Throwable t)
    {
        if (isDebugEnabled())
        {
            log.debug(msg, t);
        }
    }

    @Override
    public void info(String msg)
    {
        if (isInfoEnabled())
        {
            log.info(msg);
        }
    }

    @Override
    public void info(String format, Object arg)
    {
        if (isInfoEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            log.info(ft.getMessage());
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2)
    {
        if (isInfoEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            log.info(ft.getMessage());
        }
    }

    @Override
    public void info(String format, Object... arguments)
    {
        if (isInfoEnabled())
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            log.info(ft.getMessage());
        }
    }

    @Override
    public void info(String msg, Throwable t)
    {
        if (isInfoEnabled())
        {
            log.info(msg, t);
        }
    }

    @Override
    public void warn(String msg)
    {
        if (isWarnEnabled())
        {
            log.warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg)
    {
        if (isWarnEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            log.warn(ft.getMessage());
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2)
    {
        if (isWarnEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            log.warn(ft.getMessage());
        }
    }

    @Override
    public void warn(String format, Object... arguments)
    {
        if (isWarnEnabled())
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            log.warn(ft.getMessage());
        }
    }

    @Override
    public void warn(String msg, Throwable t)
    {
        if (isWarnEnabled())
        {
            log.warn(msg, t);
        }
    }

    @Override
    public void error(String msg)
    {
        if (isErrorEnabled())
        {
            log.error(msg);
        }
    }

    @Override
    public void error(String format, Object arg)
    {
        if (isErrorEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            log.error(ft.getMessage());
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2)
    {
        if (isErrorEnabled())
        {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            log.error(ft.getMessage());
        }
    }

    @Override
    public void error(String format, Object... arguments)
    {
        if (isErrorEnabled())
        {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
            log.error(ft.getMessage());
        }
    }

    @Override
    public void error(String msg, Throwable t)
    {
        if (isErrorEnabled())
        {
            log.error(msg, t);
        }
    }
}
