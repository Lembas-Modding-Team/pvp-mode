package pvpmode.internal.server.configuration;

import java.util.*;

import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Inject;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.internal.server.ServerProxy;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "=" + ServerConfiguration.SERVER_CONFIG_PID)
public class ServerConfigurationImpl extends AutoForgeConfigurationManager implements ServerConfiguration
{

    @Inject
    public static final ConfigurationPropertyKey<String> CSV_SEPARATOR = null;

    @Inject
    public static final ConfigurationPropertyKey<String> BLOCKED_COMMANDS = null;

    @Inject
    public static final ConfigurationPropertyKey<String> GLOBAL_CHAT_MESSAGE_PREFIX = null;

    private final ServerProxy server;

    public ServerConfigurationImpl (ServerProxy server, Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys)
    {
        super (configuration, propertyKeys,
            server.getLogger ());
        this.server = server;
    }

    @Override
    public void load ()
    {
        super.load ();

        this.validateConfigurationData ();
    }

    private void validateConfigurationData ()
    {
        String currentCSVSeparator = this.getCSVSeparator ();

        if (currentCSVSeparator.length () != 1)
        {
            logger.warning ("The CSV separator \"%s\" is invalid. The default one will be used.",
                currentCSVSeparator);
            this.properties.replace (CSV_SEPARATOR, CSV_SEPARATOR.getDefaultValue ());
        }

        Iterator<String> blockedCommandsIterator = this.getBlockedCommands ().iterator ();
        while (blockedCommandsIterator.hasNext ())
        {
            String commandName = blockedCommandsIterator.next ();
            if (commandName.trim ().isEmpty ())
            {
                blockedCommandsIterator.remove ();
            }
        }

        if (this.getGlobalChatMessagePrefix ().trim ().isEmpty ())
        {
            this.properties.replace (GLOBAL_CHAT_MESSAGE_PREFIX, GLOBAL_CHAT_MESSAGE_PREFIX.getDefaultValue ());
            logger.warning ("The global chat message prefix is empty. A default one will be used.");
        }

        if (this.isDefaultPvPModeForced () && this.isPvPTogglingEnabled ())
        {
            logger.warning ("PvP toggling is enabled and the default PvP mode forced. This is not a valid state.");
        }

        Set<String> activeCombatLoggingHandlers = this.getActiveCombatLoggingHandlers ();
        if (activeCombatLoggingHandlers.size () > 0)
        {
            Iterator<String> activatedHandlersIterator = activeCombatLoggingHandlers.iterator ();
            while (activatedHandlersIterator.hasNext ())
            {
                String handlerName = activatedHandlersIterator.next ();
                if (!server.getCombatLogManager ().isValidHandlerName (handlerName))
                {
                    logger.warning ("The combat logging handler \"%s\" is not valid.",
                        handlerName);
                    activatedHandlersIterator.remove ();
                }
            }
            if (activeCombatLoggingHandlers.isEmpty ())
            {
                logger.warning ("No valid combat logging handlers were specified. A default one will be used");
                activeCombatLoggingHandlers.add (server.getCombatLogManager ().getDefaultHandlerName ());
            }
        }
        else
        {
            logger.warning ("No combat logging handlers were specified. Combat logging will be disabled.");
        }

    }

}
