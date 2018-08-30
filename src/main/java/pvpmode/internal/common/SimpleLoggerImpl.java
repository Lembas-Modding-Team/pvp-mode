package pvpmode.internal.common;

import org.apache.logging.log4j.Logger;

import pvpmode.api.common.SimpleLogger;

public class SimpleLoggerImpl implements SimpleLogger
{

    private final Logger logger;

    public SimpleLoggerImpl (Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public String getName ()
    {
        return logger.getName ();
    }

    @Override
    public void debug (String message, Object... params)
    {
        logger.debug (String.format (message, params));
    }

    @Override
    public void debugThrowable (String message, Throwable throwable, Object... params)
    {
        logger.debug (String.format (message, params), throwable);

    }

    @Override
    public void info (String message, Object... params)
    {
        logger.info (String.format (message, params));
    }

    @Override
    public void infoThrowable (String message, Throwable throwable, Object... params)
    {
        logger.info (String.format (message, params), throwable);
    }

    @Override
    public void warning (String message, Object... params)
    {
        logger.warn (String.format (message, params));
    }

    @Override
    public void warningThrowable (String message, Throwable throwable, Object... params)
    {
        logger.warn (String.format (message, params), throwable);
    }

    @Override
    public void error (String message, Object... params)
    {
        logger.error (String.format (message, params));
    }

    @Override
    public void errorThrowable (String message, Throwable throwable, Object... params)
    {
        logger.error (String.format (message, params), throwable);
    }

}
