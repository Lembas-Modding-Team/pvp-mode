package pvpmode.modules.lotr.internal.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import pvpmode.PvPMode;
import pvpmode.api.common.*;
import pvpmode.api.common.compatibility.CompatibilityModuleLoader;
import pvpmode.api.common.configuration.Configurable;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.overrides.PvPOverrideCondition;
import pvpmode.api.server.utils.*;
import pvpmode.internal.server.ServerProxy;
import pvpmode.modules.lotr.api.common.LOTRCommonConstants;
import pvpmode.modules.lotr.api.server.*;
import pvpmode.modules.lotr.internal.common.*;
import pvpmode.modules.lotr.internal.common.network.*;
import pvpmode.modules.lotr.internal.server.gear.ServerBlockedGearManager;
import pvpmode.modules.lotr.internal.server.overrides.*;

/**
 * The server-side compatibility module for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModServerCompatibilityModule extends LOTRModCommonCompatibilityModule implements Configurable
{

    private static final String LOTR_BIOME_IDS_FILE_NAME = "lotr_mod_biome_ids.txt";
    private static final String EXTENDED_ENEMY_BIOME_CONFIG_FILE_NAME = "extended_enemy_biomes.txt";
    private static final String DEFAULT_ENEMY_BIOME_MAP_FILE_NAME = "default_enemy_biomes_map.png";

    private LOTRServerConfiguration config;

    private SafeBiomeOverrideCondition safeBiomeOverrideCondition;
    private HostileBiomeOverrideCondition hostileBiomeOverrideCondition;

    ServerBlockedGearManager blockedGearManager;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        blockedGearManager = new ServerBlockedGearManager ();

        config = this.createConfiguration (configFile ->
        {
            PvPMode.proxy.getAutoConfigManager ().processConfigurationManager (LOTRServerConfiguration.class,
                LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID);
            PvPMode.proxy.getAutoConfigManager ().processConfigurationManager (LOTRServerConfigurationImpl.class,
                LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID);
            return new LOTRServerConfigurationImpl (configFile, PvPMode.instance.getServerProxy ()
                .getAutoConfigManager ().getGeneratedKeys ().get (LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID),
                logger, this);
        });

    }

    void initEnemyBiomeOverrides (LOTRServerConfiguration config)
    {
        initBiomeOverrides (configurationFolder, LOTRServerConstants.ENEMY_BIOME_CONFIG_FILE_NAME, "lotr enemy biome",
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
        initBiomeOverrides (configurationFolder, LOTRServerConstants.SAFE_BIOME_CONFIG_FILE_NAME, "lotr safe biome",
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

    public void recreateFile (Path targetFolder, String filename, String shortName)
    {
        this.recreateFile (targetFolder, filename, filename, shortName, false);
    }

    public void recreateFile (Path targetFolder, String filename, String targetFilename, String shortName,
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
                UUID masterUUID = npc.hiredNPCInfo.getHiringPlayerUUID ();
                if (masterUUID != null)
                {
                    event.setMasterUUID (masterUUID);
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
        /*
         * Fixed that hired units eventually "attack" players with PvP Mode OFF - they
         * don't cause damage (the PvP Mode Mod prevents that), but still move to them,
         * which is canceled here.
         */
        if (event.target != null)
        {
            // The entity needs a target
            if (event.entityLiving instanceof LOTREntityNPC)
            {
                // It needs to be an hired unit
                LOTREntityNPC npc = (LOTREntityNPC) event.entityLiving;

                UUID attackingMasterUUID = PvPServerUtils.getMaster (npc);
                UUID targetMasterUUID = PvPServerUtils.getMaster (event.target);

                if (attackingMasterUUID != null && targetMasterUUID != null)
                {
                    // The attacking unit and the attacked entity have to be assignable to players
                    if (PvPServerUtils.getPvPMode (attackingMasterUUID) != EnumPvPMode.ON
                        || PvPServerUtils.getPvPMode (targetMasterUUID) != EnumPvPMode.ON)
                    {
                        // Cancel the attack target assignment of the PvP mode prevents an attack
                        npc.setAttackTarget (null);
                        npc.setRevengeTarget (null);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onInitialSent (OnInitialSupportPackageSentEvent event)
    {
        if (event.getClientData ().getLoadedCompatibilityModules ().contains (LOTRCommonConstants.LOTR_MOD_MODID))
        {
            ServerProxy server = PvPMode.instance.getServerProxy ();

            server.getPacketDispatcher ().sendTo (
                new GearItemsBlockedConfigurationChange (this.config.areGearItemsBlocked ()),
                event.getClientData ().getPlayer ());
            server.getPacketDispatcher ().sendTo (
                new BlockedGearItemsListChangedMessage (blockedGearManager.getBlockedItems ()),
                event.getClientData ().getPlayer ());
            server.getPacketDispatcher ().sendTo (
                new EquippingOfBlockedArmorBlockedConfigurationChangeMessage (
                    this.config.isEquippingOfBlockedArmorBlocked ()),
                event.getClientData ().getPlayer ());
        }
    }

    @Override
    public LOTRServerConfiguration getConfiguration ()
    {
        return config;
    }

    @Override
    public ServerBlockedGearManager getBlockedGearManager ()
    {
        return blockedGearManager;
    }

}
