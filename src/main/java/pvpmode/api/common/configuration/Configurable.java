package pvpmode.api.common.configuration;

/**
 * An interface every class which provides configuration data can implement. It
 * can be used to find classes which provide configuration data.
 * 
 * @author CraftedMods
 *
 */
public interface Configurable
{

    /**
     * Returns the configuration manager this configurable class provides. The
     * returned manager musn't be null.
     * 
     * @return The configuration manager
     */
    public ConfigurationManager getConfiguration ();

}
