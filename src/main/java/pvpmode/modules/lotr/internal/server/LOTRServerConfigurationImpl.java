package pvpmode.modules.lotr.internal.server;

import java.io.*;
import java.util.Map;

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
    public static final ConfigurationPropertyKey<?> ENEMY_BIOME_OVERRIDES_ENABLED = null;

    @Inject
    public static final ConfigurationPropertyKey<?> SAFE_BIOME_OVERRIDES_ENABLED = null;

    protected LOTRServerConfigurationImpl (Configuration configuration,
        Map<String, ConfigurationPropertyKey<?>> propertyKeys, SimpleLogger logger, LOTRModCompatibilityModule module)
    {
        super (configuration, propertyKeys, logger);
        this.module = module;
    }

    @Override
    protected void onPropertyChanged (ConfigurationPropertyKey<?> key, Object oldValue, Object newValue)
    {
        super.onPropertyChanged (key, oldValue, newValue);

        if (key == LOTRServerConfigurationImpl.ENEMY_BIOME_OVERRIDES_ENABLED)
        {
            if (this.areEnemyBiomeOverridesEnabled ())
            {
                module.initEnemyBiomeOverrides ();
            }
            else
            {
                module.removeEnemyBiomeOverrideCondition ();
            }
        }
        else if (key == LOTRServerConfigurationImpl.SAFE_BIOME_OVERRIDES_ENABLED)
        {
            if (this.areSafeBiomeOverridesEnabled ())
            {
                module.initSafeBiomeOverrides ();
            }
            else
            {
                module.removeSafeBiomeOverrideCondition ();
            }
        }
    }

    @Override
    protected void onPropertiesChanged ()
    {
        super.onPropertiesChanged ();

        module.initGeneralBiomeOverrides ();

        logger.info ("PvP mode overrides for LOTR biomes are %s",
            this.areEnemyBiomeOverridesEnabled () || this.areSafeBiomeOverridesEnabled () ? "enabled"
                : "disabled");
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
