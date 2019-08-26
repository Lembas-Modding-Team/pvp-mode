package pvpmode.modules.lotr.internal.server;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.collect.Multimap;

import lotr.common.fac.LOTRFaction;
import net.minecraftforge.common.config.Configuration;
import pvpmode.PvPMode;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.ConfigurationPropertyKey;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.Inject;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.internal.server.ServerProxy;
import pvpmode.modules.lotr.api.common.LOTRCommonConstants;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;
import pvpmode.modules.lotr.internal.common.network.*;

@Process(properties =
{AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID,
    AutoConfigurationConstants.MANUAL_PROCESSING_PROPERTY_KEY + "=true"})
public class LOTRServerConfigurationImpl extends AutoForgeConfigurationManager implements LOTRServerConfiguration
{

    private final LOTRModServerCompatibilityModule module;

    private final ServerProxy server;

    @Inject
    public static final ConfigurationPropertyKey<Multimap<String, String>> FACTION_PLACEHOLDERS = null;

    @Inject
    public static final ConfigurationPropertyKey<Boolean> GEAR_ITEMS_BLOCKED = null;

    @Inject
    public static final ConfigurationPropertyKey<Boolean> EQUIPPING_OF_BLOCKED_ARMOR_BLOCKED = null;

    protected LOTRServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger,
        LOTRModServerCompatibilityModule module)
    {
        super (configuration, propertyKeys, logger);
        this.module = module;
        this.server = PvPMode.instance.getServerProxy ();
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

        if (this.areGearItemsBlocked ())
        {
            module.blockedGearManager.init (module, this);
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
                        "The faction placeholder \"%s\" contained a circular reference - the whole placeholder will be removed",
                        entryKey);
                    keysIterator.remove ();
                }
            }

            logger.info ("Loaded %s faction placeholders", placeholders.keySet ().size ());

        }
        else if (key == GEAR_ITEMS_BLOCKED)
        {
            PvPServerUtils.getClientsWithCompatibilityModule (LOTRCommonConstants.LOTR_MOD_MODID).forEach (clientData ->
            {
                server.getPacketDispatcher ().sendTo (
                    new GearItemsBlockedConfigurationChange (this.areGearItemsBlocked ()),
                    clientData.getPlayer ());
            });

        }
        else if (key == EQUIPPING_OF_BLOCKED_ARMOR_BLOCKED)
        {
            PvPServerUtils.getClientsWithCompatibilityModule (LOTRCommonConstants.LOTR_MOD_MODID).forEach (clientData ->
            {
                server.getPacketDispatcher ().sendTo (
                    new EquippingOfBlockedArmorBlockedConfigurationChangeMessage (
                        this.isEquippingOfBlockedArmorBlocked ()),
                    clientData.getPlayer ());
            });

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
