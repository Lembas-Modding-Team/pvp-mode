package pvpmode.modules.lotr.internal.server;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.collect.Multimap;

import lotr.common.LOTRFaction;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Inject;
import pvpmode.api.common.utils.Process;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;

@Process(properties = AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID)
public class LOTRServerConfigurationImpl extends AutoForgeConfigurationManager implements LOTRServerConfiguration
{

    private final LOTRModCompatibilityModule module;

    @Inject
    public static final ConfigurationPropertyKey<Multimap<String, String>> FACTION_PLACEHOLDERS = null;

    protected LOTRServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger, LOTRModCompatibilityModule module)
    {
        super (configuration, propertyKeys, logger);
        this.module = module;
    }

    @Override
    protected void onPropertiesChanged ()
    {
        super.onPropertiesChanged ();

        module.initGeneralBiomeOverrides ();

        if (this.areEnemyBiomeOverridesEnabled ())
        {
            module.initEnemyBiomeOverrides (this);
        }
        else
        {
            module.removeEnemyBiomeOverrideCondition ();
        }

        if (this.areSafeBiomeOverridesEnabled ())
        {
            module.initSafeBiomeOverrides (this);
        }
        else
        {
            module.removeSafeBiomeOverrideCondition ();
        }

        logger.info ("PvP mode overrides for LOTR biomes are %s",
            this.areEnemyBiomeOverridesEnabled () || this.areSafeBiomeOverridesEnabled () ? "enabled"
                : "disabled");
    }

    @Override
    protected void onPropertyChanged (ConfigurationPropertyKey<?> key, Object oldValue, Object newValue)
    {
        super.onPropertyChanged (key, oldValue, newValue);

        if (key == FACTION_PLACEHOLDERS)
        {
            Multimap<String, String> placeholders = this.getFactionPlaceholders ();

            Set<String> removedPlaceholders = new HashSet<> ();

            Iterator<String> keysIterator = placeholders.asMap ().keySet ().iterator ();

            SimpleLogger logger = module.getLogger ();

            // Remove placeholders with invalid name
            while (keysIterator.hasNext ())
            {
                String placeholderName = keysIterator.next ().trim ();

                boolean remove = false;

                if (!Pattern.matches ("^([A-Z]|_)+$", placeholderName))
                {
                    remove = true;
                    removedPlaceholders.add (placeholderName);
                    logger.warning (
                        "Removed the invalid faction placeholder \"%s\" (it mustn't be empty, and must only contain uppercase letters or the underscore)",
                        placeholderName);
                }
                else if (LOTRFaction.forName (placeholderName) != null)
                {
                    remove = true;
                    logger.warning ("Removed an faction placeholder with the same name as the LOTR faction %s",
                        placeholderName);
                }

                if (remove)
                {
                    keysIterator.remove ();
                }
            }

            /*
             * Remove all invalid references from the remaining placeholders. Needs to be a
             * second step so that all invalid placeholders that were determined in the
             * first step were detected.
             */
            for (String placeholderName : new HashSet<> (placeholders.keySet ()))
            {
                Iterator<String> iterator2 = placeholders.get (placeholderName).iterator ();

                while (iterator2.hasNext ())
                {
                    String referencedFactionOrPlaceholder = iterator2.next ().trim ();

                    boolean remove = false;

                    if (removedPlaceholders.contains (referencedFactionOrPlaceholder))
                    {
                        logger.warning (
                            "The faction placeholder \"%s\" contains a reference to the faction placeholder \"%s\" - which was removed. The reference will be deleted.",
                            placeholderName, referencedFactionOrPlaceholder);
                        remove = true;
                    }
                    else if (LOTRFaction.forName (referencedFactionOrPlaceholder) == null
                        && !placeholders.keySet ().contains (referencedFactionOrPlaceholder))
                    {
                        logger.warning (
                            "The faction placeholder \"%s\" contains a reference to \"%s\" - but that is neither a faction nor a placeholder. The reference will be deleted.",
                            placeholderName, referencedFactionOrPlaceholder);
                        remove = true;
                    }

                    if (remove)
                    {
                        iterator2.remove ();
                    }
                }
            }

            // Detect circular references
            keysIterator = placeholders.keySet ().iterator ();

            while (keysIterator.hasNext ())
            {
                String entryKey = keysIterator.next ();

                if (scanPlaceholders (placeholders.asMap (), entryKey, new HashSet<> ()))
                {
                    this.logger.warning (
                        "The faction placeholder \"%s\" contained a reference which lead to a circle - the whole placeholder will be removed",
                        entryKey);
                    keysIterator.remove ();
                }
            }

            logger.info ("Loaded %s faction placeholders", placeholders.keySet ().size ());

        }
    }

    private boolean scanPlaceholders (Map<String, Collection<String>> placeholderMap, String currentPlaceholder,
        Set<String> visitedPlaceholders)
    {
        visitedPlaceholders.add (currentPlaceholder);

        for (String referenceName : placeholderMap.get (currentPlaceholder))
        {
            if (visitedPlaceholders.contains (referenceName))
                return true;
            if (placeholderMap.containsKey (referenceName))
            {
                if (scanPlaceholders (placeholderMap, referenceName, visitedPlaceholders))
                    return true;
            }
        }
        return false;
    }

    @Override
    protected InputStream openDisplayNameInputStream () throws IOException
    {
        return this.getClass ()
            .getResourceAsStream ("/assets/pvpmode/modules/lotr/configurationDisplayNames.properties");
    }

    @Override
    protected InputStream openCommentInputStream () throws IOException
    {
        return this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/configurationComments.properties");
    }

}
