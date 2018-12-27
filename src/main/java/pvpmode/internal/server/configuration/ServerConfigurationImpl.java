package pvpmode.internal.server.configuration;

import java.io.*;
import java.util.*;

import cpw.mods.fml.common.Loader;
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

    @Inject
    public static final ConfigurationPropertyKey<Boolean> SOULBOUND_ITEMS_ENABLED = null;

    @Inject
    public static final ConfigurationPropertyKey<String> SOULBOUND_ITEM_TOOLTIP = null;

    @Inject
    public static final ConfigurationPropertyKey<String> DEFAULT_PVP_MODE_FORCED = null;

    @Inject
    public static final ConfigurationPropertyKey<String> PVP_TOGGLING_ENABLED = null;

    @Inject
    public static final ConfigurationPropertyKey<String> ACTIVE_COMBAT_LOGGING_HANDLERS = null;

    private final ServerProxy server;

    public ServerConfigurationImpl (ServerProxy server, Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys)
    {
        super (configuration, propertyKeys,
            server.getLogger ());
        this.server = server;
    }

    @Override
    protected void onPropertyChanged (ConfigurationPropertyKey<?> key, Object oldValue, Object newValue)
    {
        super.onPropertyChanged (key, oldValue, newValue);

        if (key == CSV_SEPARATOR)
        {
            String currentCSVSeparator = this.getCSVSeparator ();

            if (currentCSVSeparator.length () != 1 || currentCSVSeparator.trim ().isEmpty ())
            {
                logger.warning ("The CSV separator \"%s\" is invalid. The default one will be used.",
                    currentCSVSeparator);
                this.properties.replace (CSV_SEPARATOR, CSV_SEPARATOR.getDefaultValue ());
            }
        }
        else if (key == BLOCKED_COMMANDS)
        {
            Iterator<String> blockedCommandsIterator = this.getBlockedCommands ().iterator ();
            while (blockedCommandsIterator.hasNext ())
            {
                String commandName = blockedCommandsIterator.next ();
                if (commandName.trim ().isEmpty ())
                {
                    blockedCommandsIterator.remove ();
                }
            }
        }
        else if (key == GLOBAL_CHAT_MESSAGE_PREFIX)
        {
            if (this.getGlobalChatMessagePrefix ().trim ().isEmpty ())
            {
                this.properties.replace (GLOBAL_CHAT_MESSAGE_PREFIX, GLOBAL_CHAT_MESSAGE_PREFIX.getDefaultValue ());
                logger.warning ("The global chat message prefix is empty. A default one will be used.");
            }
        }
        else if (key == DEFAULT_PVP_MODE_FORCED || key == PVP_TOGGLING_ENABLED)
        {
            if (this.isDefaultPvPModeForced () && this.isPvPTogglingEnabled ())
            {
                logger.warning ("PvP toggling is enabled and the default PvP mode forced. This is not a valid state.");
            }
        }// Compare by internal name because the key was replaced
        else if (key.getInternalName ().equals (ACTIVE_COMBAT_LOGGING_HANDLERS.getInternalName ()))
        {
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
        else if (key == SOULBOUND_ITEMS_ENABLED)
        {
            if (Loader.isModLoaded ("suffixforge"))
            {
                this.properties.replace (SOULBOUND_ITEMS_ENABLED, false);
                logger.info ("SuffixForge is present - the soulbound command will be disabled");
            }
        }
        else if (key == SOULBOUND_ITEM_TOOLTIP)
        {
            if (this.getProperty (SOULBOUND_ITEM_TOOLTIP).trim ().isEmpty ())
            {
                properties.replace (SOULBOUND_ITEM_TOOLTIP, SOULBOUND_ITEM_TOOLTIP.getDefaultValue ());
                logger.warning ("The soulbound tooltip is empty. A default one will be used.");
            }
        }
    }

    @Override
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ().getResourceAsStream ("/assets/pvpmode/configurationDisplayNames.properties");
    }

    @Override
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ().getResourceAsStream ("/assets/pvpmode/configurationComments.properties");
    }

}
