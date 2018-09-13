package pvpmode.modules.lotr.internal.server;

import static pvpmode.modules.lotr.api.server.LOTRServerConfigurationConstants.*;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import pvpmode.PvPMode;
import pvpmode.api.common.*;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.compatibility.events.*;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.overrides.PvPOverrideCondition;
import pvpmode.api.server.utils.*;

/**
 * The compatibility module for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModule extends AbstractCompatibilityModule
{

    private static final String ENEMY_BIOME_CONFIG_FILE_NAME = "pvpmode_lotr_enemy_biomes.txt";
    private static final String LOTR_BIOME_IDS_FILE_NAME = "lotr_mod_biome_ids.txt";
    private static final String EXTENDED_ENEMY_BIOME_CONFIG_FILE_NAME = "extended_enemy_biomes.txt";
    private static final String DEFAULT_ENEMY_BIOME_MAP_FILE_NAME = "default_enemy_biomes_map.png";
    private static final String SAFE_BIOME_CONFIG_FILE_NAME = "pvpmode_lotr_safe_biomes.txt";

    private boolean areEnemyBiomeOverridesEnabled;
    private boolean blockFTInPvP;
    private boolean dropSkullWithKeepInventory;
    private boolean areSafeBiomeOverridesEnabled;

    private LOTREventHandler eventHandler;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        Configuration configuration = this.getDefaultConfiguration ();

        areEnemyBiomeOverridesEnabled = configuration.getBoolean (
            ENEMY_BIOME_OVERRIDES_ENABLED_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, the PvP mode enemy biome override condition for LOTR biomes will be enabled. Players who are an enemy of a faction are forced to have PvP enabled while they're in a biome which is clearly assignable to that faction. This is highly configurable.");
        blockFTInPvP = configuration.getBoolean (BLOCK_FAST_TRAVELING_WHILE_PVP_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY,
            true, "If enabled, players cannot use the LOTR fast travel system while they're in PvP.");
        dropSkullWithKeepInventory = configuration.getBoolean (ALWAYS_DROP_PLAYER_SKULLS_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, true,
            "If true, players killed with a weapon with the headhunting modifier will drop their skulls even with keepInventory enabled.");
        areSafeBiomeOverridesEnabled = configuration.getBoolean (
            SAFE_BIOME_OVERRIDES_ENABLED_CONFIGURATION_NAME,
            ServerConfiguration.SERVER_CONFIGURATION_CATEGORY, false,
            "If true, the PvP mode safe override condition for LOTR biomes will be enabled, which has a higher priority than the enemy override condition. Players who are aligned with a faction are forced to have PvP disabled while they're in a biome which is clearly assignable to that faction. This can also be applied without the alignment criterion. This is highly configurable.");

        if (configuration.hasChanged ())
        {
            configuration.save ();
        }

        logger.info ("PvP mode overrides for LOTR biomes are %s",
            areEnemyBiomeOverridesEnabled || areSafeBiomeOverridesEnabled ? "enabled" : "disabled");

        if (areEnemyBiomeOverridesEnabled)
        {
            initEnemyBiomeOverrides (configurationFolder);
        }
        if (areSafeBiomeOverridesEnabled)
        {
            initSafeBiomeOverrides (configurationFolder);
        }
        if (areEnemyBiomeOverridesEnabled || areSafeBiomeOverridesEnabled)
        {
            initGeneralBiomeOverrides (configurationFolder);
        }

        retrieveLOTREventHandler ();
    }

    private void initEnemyBiomeOverrides (Path configurationFolder) throws IOException
    {
        initBiomeOverrides (configurationFolder, ENEMY_BIOME_CONFIG_FILE_NAME, "lotr enemy biome",
            "default_enemy_biomes.txt", (data) -> new HostileBiomeOverrideCondition (data));

        // (Re)Create the extended enemy biome config file
        recreateFile (configurationFolder.getParent ().getParent ().getParent (),// TODO temporary
            EXTENDED_ENEMY_BIOME_CONFIG_FILE_NAME,
            "LOTR extended enemy biomes configuration file template");
        // (Re)Create the default config map image
        recreateFile (configurationFolder.getParent ().getParent ().getParent (),
            DEFAULT_ENEMY_BIOME_MAP_FILE_NAME, "LOTR default enemy biome map");
    }

    private void initSafeBiomeOverrides (Path configurationFolder) throws IOException
    {
        initBiomeOverrides (configurationFolder, SAFE_BIOME_CONFIG_FILE_NAME, "lotr safe biome",
            "default_safe_biomes.txt", (data) -> new SafeBiomeOverrideCondition (data));
    }

    private void initBiomeOverrides (Path configurationFolder, String configFileName, String configName,
        String defaultConfigFileName,
        Function<Map<Integer, Collection<BiomeFactionEntry>>, PvPOverrideCondition> conditionCreator)
        throws IOException
    {
        Path biomeConfigurationFile = configurationFolder.resolve (configFileName);

        // Recreate the config file if it doesn't exist
        if (!Files.exists (biomeConfigurationFile))
        {
            logger.info ("The %s configuration file doesn't exist - it'll be created", configName);
            Files.createFile (biomeConfigurationFile);

            PvPCommonUtils.writeFromStreamToFile (
                () -> this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/" + defaultConfigFileName),
                biomeConfigurationFile);
        }

        BiomeOverrideConfigParser parser = new BiomeOverrideConfigParser (configName,
            biomeConfigurationFile, logger);

        PvPMode.instance.getServerProxy ().getOverrideManager ()
            .registerOverrideCondition (conditionCreator.apply (parser.parse ()));
    }

    private void initGeneralBiomeOverrides (Path configurationFolder) throws IOException
    {
        // (Re)Create the LOTR biome id file
        recreateFile (configurationFolder.getParent ().getParent ().getParent (), LOTR_BIOME_IDS_FILE_NAME,
            "LOTR biome id file");// TODO temporary
    }

    private void recreateFile (Path targetFolder, String filename, String shortName) throws IOException
    {
        Path file = targetFolder.resolve (filename);
        if (!Files.exists (file))
        {
            logger.info ("The %s doesn't exist - it'll be created", shortName);
            Files.createFile (file);
        }

        PvPCommonUtils.writeFromStreamToFile (
            () -> this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/" + filename),
            file);
        logger.info ("Recreated the %s", shortName);
    }

    private void retrieveLOTREventHandler ()
    {
        try
        {
            Field eventHandlerField = LOTRMod.class.getDeclaredField ("modEventHandler");
            eventHandlerField.setAccessible (true);
            eventHandler = (LOTREventHandler) eventHandlerField.get (null);
            if (eventHandler != null)
            {
                logger.info ("Successfully retrieved the LOTR event handler");
            }
            else
            {
                logger.warning ("Couldn't retrieve the LOTR event handler - features depending on it won't be enabled");
            }
        }
        catch (Exception e)
        {
            logger.errorThrowable ("Couldn't retrieve the LOTR event handler", e);
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
        if (blockFTInPvP)
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

    @SubscribeEvent
    public void onPlayerDeath (LivingDeathEvent event)
    {
        if (dropSkullWithKeepInventory && eventHandler != null
            && event.entityLiving.worldObj.getGameRules ().getGameRuleBooleanValue ("keepInventory"))
        {
            if (event.entityLiving instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) event.entityLiving;
                // The last two parameters are not used by the current implementation
                eventHandler.onPlayerDrops (new PlayerDropsEvent (player, event.source, null, false));
            }
        }
    }

}
