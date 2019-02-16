package pvpmode.modules.lotr.internal.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import pvpmode.PvPMode;
import pvpmode.api.common.*;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.configuration.*;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.overrides.PvPOverrideCondition;
import pvpmode.api.server.utils.*;
import pvpmode.modules.lotr.api.server.LOTRServerConfiguration;
import pvpmode.modules.lotr.internal.server.overrides.*;

/**
 * The compatibility module for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModule extends AbstractCompatibilityModule implements Configurable
{

    private static final String ENEMY_BIOME_CONFIG_FILE_NAME = "pvpmode_lotr_enemy_biomes.txt";
    private static final String LOTR_BIOME_IDS_FILE_NAME = "lotr_mod_biome_ids.txt";
    private static final String EXTENDED_ENEMY_BIOME_CONFIG_FILE_NAME = "extended_enemy_biomes.txt";
    private static final String DEFAULT_ENEMY_BIOME_MAP_FILE_NAME = "default_enemy_biomes_map.png";
    private static final String SAFE_BIOME_CONFIG_FILE_NAME = "pvpmode_lotr_safe_biomes.txt";

    private LOTRServerConfiguration config;

    private SafeBiomeOverrideCondition safeBiomeOverrideCondition;
    private HostileBiomeOverrideCondition hostileBiomeOverrideCondition;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        config = this.createConfiguration (configFile ->
        {
            return new LOTRServerConfigurationImpl (configFile, PvPMode.instance.getServerProxy ()
                .getAutoConfigManager ().getGeneratedKeys ().get (LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID),
                logger, this);
        });

    }

    void initEnemyBiomeOverrides (LOTRServerConfiguration config)
    {
        initBiomeOverrides (configurationFolder, ENEMY_BIOME_CONFIG_FILE_NAME, "lotr enemy biome",
            "default_enemy_biomes.txt", (data) -> new HostileBiomeOverrideCondition (data),
            () -> hostileBiomeOverrideCondition,
            (condition) -> hostileBiomeOverrideCondition = (HostileBiomeOverrideCondition) condition, config);

        // (Re)Create the extended enemy biome config file
        recreateFile (PvPMode.proxy.getGeneratedFilesFolder (),
            EXTENDED_ENEMY_BIOME_CONFIG_FILE_NAME,
            "LOTR extended enemy biomes configuration file template");
        // (Re)Create the default config map image
        recreateFile (PvPMode.proxy.getGeneratedFilesFolder (),
            DEFAULT_ENEMY_BIOME_MAP_FILE_NAME, "LOTR default enemy biome map");
    }

    void removeEnemyBiomeOverrideCondition ()
    {
        if (hostileBiomeOverrideCondition != null)
        {
            PvPMode.instance.getServerProxy ().getOverrideManager ()
                .unregisterOverrideCondition (hostileBiomeOverrideCondition);
        }
    }

    void removeSafeBiomeOverrideCondition ()
    {
        if (safeBiomeOverrideCondition != null)
        {
            PvPMode.instance.getServerProxy ().getOverrideManager ()
                .unregisterOverrideCondition (safeBiomeOverrideCondition);
        }
    }

    void initSafeBiomeOverrides (LOTRServerConfiguration config)
    {
        initBiomeOverrides (configurationFolder, SAFE_BIOME_CONFIG_FILE_NAME, "lotr safe biome",
            "default_safe_biomes.txt", (data) -> new SafeBiomeOverrideCondition (data),
            () -> safeBiomeOverrideCondition,
            (condition) -> safeBiomeOverrideCondition = (SafeBiomeOverrideCondition) condition, config);
    }

    private void initBiomeOverrides (Path configurationFolder, String configFileName, String configName,
        String defaultConfigFileName,
        Function<Map<Integer, Collection<FactionEntry>>, PvPOverrideCondition> conditionCreator,
        Supplier<PvPOverrideCondition> currentConditionGetter, Consumer<PvPOverrideCondition> currentConditionSetter,
        LOTRServerConfiguration config)
    {
        this.recreateFile (configurationFolder, defaultConfigFileName, configFileName,
            configName + " configuration file", true);

        BiomeOverrideConfigParser parser = new BiomeOverrideConfigParser (configName,
            configurationFolder.resolve (configFileName), logger, config);

        try
        {

            parser.parse ();

            PvPOverrideCondition condition = conditionCreator.apply (parser.getParsedData ());

            if (currentConditionGetter.get () != null)
            {
                PvPMode.instance.getServerProxy ().getOverrideManager ()
                    .unregisterOverrideCondition (currentConditionGetter.get ());
            }

            if (PvPMode.instance.getServerProxy ().getOverrideManager ()
                .registerOverrideCondition (condition))
                currentConditionSetter.accept (condition);
        }
        catch (IOException e)
        {
            this.logger.errorThrowable ("Couldn't parse the %s", e, configName);
        }

    }

    void initGeneralBiomeOverrides ()
    {
        // (Re)Create the LOTR biome id file
        recreateFile (PvPMode.proxy.getGeneratedFilesFolder (), LOTR_BIOME_IDS_FILE_NAME,
            "LOTR biome id file");
    }

    private void recreateFile (Path targetFolder, String filename, String shortName)
    {
        this.recreateFile (targetFolder, filename, filename, shortName, false);
    }

    private void recreateFile (Path targetFolder, String filename, String targetFilename, String shortName,
        boolean recreateIfNotExists)
    {
        try
        {
            Path file = targetFolder.resolve (targetFilename);

            boolean existed = true;

            if (!Files.exists (file))
            {
                logger.info ("The %s doesn't exist - it'll be created", shortName);
                Files.createFile (file);
                existed = false;
            }

            if (recreateIfNotExists && !existed || !recreateIfNotExists)
            {
                PvPCommonUtils.writeFromStreamToFile (
                    () -> this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/" + filename),
                    file);
                logger.info ("Recreated the %s", shortName);
            }
        }
        catch (IOException e)
        {
            logger.errorThrowable ("Couldn't recreate the %s", e, shortName);
        }
    }

    @SubscribeEvent
    public void onEntityMasterExtraction (EntityMasterExtractionEvent event)
    {
        Entity entity = event.getEntity ();
        if (entity instanceof LOTREntityNPC)
        {
            LOTREntityNPC npc = (LOTREntityNPC) entity;
            if (npc.hiredNPCInfo.isActive)
            {
                EntityPlayer master = npc.hiredNPCInfo.getHiringPlayer ();
                if (master != null)
                {
                    event.setMaster ((EntityPlayerMP) master);
                }
            }
        }

    }

    @SubscribeEvent
    public void onPlayerPvPTick (PlayerPvPTickEvent event)
    {
        if (config.isFastTravelingWhilePvPBlocked ())
        {
            LOTRPlayerData data = LOTRLevelData.getData (event.getPlayer ());
            if (data.getTargetFTWaypoint () != null)
            {
                ServerChatUtils.red (event.getPlayer (), "You cannot fast travel while in PvP combat");
                data.setTargetFTWaypoint (null);
            }
        }
    }

    @SubscribeEvent
    public void onAttackTargetSet (LivingSetAttackTargetEvent event)
    {
        // Fixes that hired units attack players (they don't cause damage, but move to
        // them)
        if (event.target != null)
        {
            // The entity needs a target
            if (event.entityLiving instanceof LOTREntityNPC)
            {
                // It needs to be an hired unit
                LOTREntityNPC npc = (LOTREntityNPC) event.entityLiving;

                EntityPlayer attackingMaster = PvPServerUtils.getMaster (npc);
                EntityPlayer targetMaster = PvPServerUtils.getMaster (event.target);

                if (attackingMaster != null && targetMaster != null)
                {
                    // The attacking unit and the attacked entity have to be assignable to players
                    if (PvPServerUtils.getPvPMode (attackingMaster) != EnumPvPMode.ON
                        || PvPServerUtils.getPvPMode (targetMaster) != EnumPvPMode.ON)
                    {
                        // Cancel the attack target assignment of the PvP mode prevents an attack
                        npc.setAttackTarget (null);
                        npc.setRevengeTarget (null);
                    }
                }
            }
        }
    }

    @Override
    public ConfigurationManager getConfiguration ()
    {
        return config;
    }

}
