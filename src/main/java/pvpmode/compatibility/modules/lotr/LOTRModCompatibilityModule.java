package pvpmode.compatibility.modules.lotr;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.*;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import pvpmode.*;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.*;

/**
 * The compatibility module for the LOTR Mod.
 * 
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModule implements CompatibilityModule
{

    private static final String LOTR_CONFIGURATION_CATEGORY = "LOTR_MOD_COMPATIBILITY";
    private static final String ENEMY_BIOME_CONFIG_FILE_NAME = "pvpmode_lotr_enemy_biomes.txt";
    private static final String LOTR_BIOME_IDS_FILE_NAME = "lotr_mod_biome_ids.txt";

    private boolean areEnemyBiomeOverridesEnabled;
    private boolean blockFTInPvP;
    private boolean dropSkullWithKeepInventory;

    private LOTREventHandler eventHandler;

    @Override
    public void load () throws IOException
    {
        MinecraftForge.EVENT_BUS.register (this);

        areEnemyBiomeOverridesEnabled = PvPMode.config.getBoolean ("Enable enemy biome override condition",
            LOTR_CONFIGURATION_CATEGORY, true,
            "If true, the PvP mode enemy biome override condition for LOTR biomes will be enabled. Players who are an enemy of a faction are forced to have PvP enabled while they're in a biome which is clearly assignable to that faction. This is highly configurable.");
        blockFTInPvP = PvPMode.config.getBoolean ("Block fast traveling in PvP", LOTR_CONFIGURATION_CATEGORY,
            true, "If enabled, players cannot use the LOTR fast travel system while they're in PvP.");
        dropSkullWithKeepInventory = PvPMode.config.getBoolean ("Always Drop Player Skulls",
            LOTR_CONFIGURATION_CATEGORY, true,
            "If true, players killed with a weapon with the headhunting modifier will drop their skulls even with keepInventory enabled.");

        PvPMode.config.addCustomCategoryComment (LOTR_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"The Lord of the Rings Minecraft Mod\"");

        Path configurationFolder = PvPMode.config.getConfigFile ().getParentFile ().toPath ();

        FMLLog.info (String.format ("PvP mode overrides for LOTR biomes are %s",
            areEnemyBiomeOverridesEnabled ? "enabled" : "disabled"));
        if (areEnemyBiomeOverridesEnabled)
        {
            initEnemyBiomeOverrides (configurationFolder);
        }

        this.retrieveLOTREventHandler ();
    }

    private void initEnemyBiomeOverrides (Path configurationFolder) throws IOException
    {
        Path enemyBiomeConfigurationFile = configurationFolder.resolve (ENEMY_BIOME_CONFIG_FILE_NAME);

        // Recreate the config file if it doesn't exist
        if (!Files.exists (enemyBiomeConfigurationFile))
        {
            FMLLog.getLogger ().info ("The lotr enemy biome configuration file doesn't exist - it'll be created");
            Files.createFile (enemyBiomeConfigurationFile);

            PvPUtils.writeFromStreamToFile (
                () -> this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/default_enemy_biomes.txt"),
                enemyBiomeConfigurationFile);
        }

        Map<Integer, Collection<EnemyBiomeFactionEntry>> configurationData = new HashMap<> ();

        // Parse and load the config file
        int validEntryCounter = 0;
        int invalidEntryCounter = 0;
        Set<String> readValidFactionEntries = new HashSet<> ();
        try (BufferedReader reader = Files.newBufferedReader (enemyBiomeConfigurationFile))
        {
            // Read the file line by line
            String line = null;
            for (int i = 1; (line = reader.readLine ()) != null; i++)
            {
                line = line.trim ();
                // Ignore comments and empty lines
                if (!line.isEmpty () && !line.startsWith ("#"))
                {
                    // Split config entries into three columns
                    String[] parts = line.split (";");
                    if (parts.length != 3)
                    {
                        // There are more or less than three columns
                        ++invalidEntryCounter;
                        FMLLog.warning (
                            "The lotr enemy biome config entry \"%s\" at line %d is invalid. There're too much or too less columns separated by semicolons!",
                            line,
                            i);
                    }
                    else
                    {
                        // Extract the faction identifier from the first column
                        String faction = parts[0].trim ();
                        if (faction.equals ("ALL") || LOTRFaction.forName (faction) != null)
                        {
                            if (readValidFactionEntries.contains (faction))
                            {
                                // The faction was specified already
                                ++invalidEntryCounter;
                                FMLLog.warning (
                                    "The lotr enemy biome config entry at line %d references a faction (\"%s\") which was referenced by an entry loaded before. It'll be ignored.",
                                    i, faction);
                            }
                            else
                            {
                                String alignmentString = parts[1].trim ();
                                try
                                {
                                    // Extract the minimum alignment from the
                                    // second column
                                    Integer alignmentInt = Integer.parseInt (alignmentString);

                                    // Extract the biome ids from the first
                                    // column
                                    String[] biomeIds = parts[2].trim ().split (",");
                                    if (biomeIds.length <= 0)
                                    {
                                        // No biomes were specified
                                        ++invalidEntryCounter;
                                        FMLLog.warning (
                                            "The lotr enemy biome config entry at line %d contains no assigned biome ids",
                                            i);
                                    }
                                    else
                                    {
                                        // Parse the biome ids
                                        Collection<Integer> biomeIdsInt = new HashSet<> ();
                                        for (String biomeString : biomeIds)
                                        {
                                            String biomeStringTrimmed = biomeString.trim ();
                                            try
                                            {
                                                if (!biomeIdsInt.add (Integer.parseInt (biomeStringTrimmed)))
                                                {
                                                    // Duplicated biome ids
                                                    // specified
                                                    FMLLog.warning (
                                                        "The lotr enemy biome config entry at line %d contains a duplicated biome id (%s).",
                                                        i, biomeStringTrimmed);
                                                }
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                FMLLog.warning (
                                                    "The lotr enemy biome config entry at line %d contains an invalid biome id (\"%s\"). The invalid id will be ignored.",
                                                    i, biomeStringTrimmed);
                                            }
                                        }
                                        if (biomeIdsInt.isEmpty ())
                                        {
                                            // Biome ids were specified, but all
                                            // were invalid
                                            ++invalidEntryCounter;
                                            FMLLog.warning (
                                                "The lotr enemy biome config entry at line %d contains only invalid biome ids. The entry will be ignored.",
                                                i);
                                        }
                                        else
                                        {
                                            // Add the data to our data
                                            // structured
                                            readValidFactionEntries.add (faction);
                                            EnemyBiomeFactionEntry entry = new EnemyBiomeFactionEntry (faction,
                                                alignmentInt);
                                            for (Integer biomeId : biomeIdsInt)
                                            {
                                                if (!configurationData.containsKey (biomeId))
                                                    configurationData.put (biomeId, new HashSet<> ());
                                                configurationData.get (biomeId).add (entry);
                                            }
                                            ++validEntryCounter;
                                        }
                                    }
                                }
                                catch (NumberFormatException e)
                                {
                                    ++invalidEntryCounter;
                                    FMLLog.warning (
                                        "The lotr enemy biome config entry at line %d contains an invalid minimum alignment (\"%s\")",
                                        i, alignmentString);
                                }
                            }
                        }
                        else
                        {
                            // The faction name is invalid
                            ++invalidEntryCounter;
                            FMLLog.warning (
                                "The lotr enemy biome config entry at line %d contains an invalid faction name (\"%s\").",
                                i, faction);
                        }
                    }
                }
            }
        }

        FMLLog.info ("Loaded %d of %d specified lotr enemy biome config entries. %d config entries are invalid.",
            validEntryCounter, invalidEntryCounter + validEntryCounter,
            invalidEntryCounter);

        PvPMode.overrideManager.registerOverrideCondition (new MiddleEarthBiomeOverrideCondition (configurationData));

        // (Re)Create the LOTR biome id file
        Path biomeIdFile = configurationFolder.getParent ().resolve (LOTR_BIOME_IDS_FILE_NAME);
        if (!Files.exists (biomeIdFile))
        {
            FMLLog.getLogger ().info ("The LOTR biome id file doesn't exist - it'll be created");
            Files.createFile (biomeIdFile);
        }

        PvPUtils.writeFromStreamToFile (
            () -> this.getClass ().getResourceAsStream ("/assets/pvpmode/modules/lotr/" + LOTR_BIOME_IDS_FILE_NAME),
            biomeIdFile);
        FMLLog.info ("Recreated the LOTR biome id file");
    }

    private void retrieveLOTREventHandler ()
    {
        try
        {
            Field eventHandlerField = LOTRMod.class.getDeclaredField ("modEventHandler");
            eventHandlerField.setAccessible (true);
            this.eventHandler = (LOTREventHandler) eventHandlerField.get (null);
            if (this.eventHandler != null)
            {
                FMLLog.info ("Successfully retrieved the LOTR event handler");
            }
            else
            {
                FMLLog.warning ("Couldn't retrieve the LOTR event handler - features depending on it won't be enabled");
            }
        }
        catch (Exception e)
        {
            FMLLog.getLogger ().error ("Couldn't retrieve the LOTR event handler", e);
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
                PvPUtils.red (event.getPlayer (), "You cannot fast travel while in PvP");
                data.setTargetFTWaypoint (null);
            }
        }
    }

    @SubscribeEvent
    public void onAttackTargetSet (LivingSetAttackTargetEvent event)
    {
        // Fixes that hired units attack players (they don't cause damage, but
        // move to them)
        if (event.target != null)
        {
            // The entity needs a target
            if (event.entityLiving instanceof LOTREntityNPC)
            {
                // It needs to be an hired unit
                LOTREntityNPC npc = (LOTREntityNPC) event.entityLiving;

                EntityPlayer attackingMaster = PvPUtils.getMaster (npc);
                EntityPlayer targetMaster = PvPUtils.getMaster (event.target);

                if (attackingMaster != null && targetMaster != null)
                {
                    // The attacking unit and the attacked entity have to be
                    // assignable to players
                    if (PvPUtils.getPvPMode (attackingMaster) != EnumPvPMode.ON
                        || PvPUtils.getPvPMode (targetMaster) != EnumPvPMode.ON)
                    {
                        // Cancel the attack target assignment of the PvP mode
                        // prevents an attack
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
                this.eventHandler.onPlayerDrops (new PlayerDropsEvent (player, event.source, null, false));
            }
        }
    }

}
