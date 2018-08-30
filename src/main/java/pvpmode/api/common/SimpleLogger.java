package pvpmode.api.common;

/**
 * A simple wrapper for a logger using String.format for formatting the log
 * messages. The [...]Throwable functions are there to avoid bugs related to the
 * varargs.
 *
 * @author CraftedMods
 *
 */
public interface SimpleLogger
{

    public String getName ();

    public void debug (String message, Object... params);

    public void debugThrowable (String message, Throwable throwable, Object... params);

    public void info (String message, Object... params);

    public void infoThrowable (String message, Throwable throwable, Object... params);

    public void warning (String message, Object... params);

    public void warningThrowable (String message, Throwable throwable, Object... params);

    public void error (String message, Object... params);

    public void errorThrowable (String message, Throwable throwable, Object... params);

}
