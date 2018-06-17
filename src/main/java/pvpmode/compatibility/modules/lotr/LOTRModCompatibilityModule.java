package pvpmode.compatibility.modules.lotr;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRFaction;
import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.*;
import pvpmode.compatibility.CompatibilityModule;
import pvpmode.compatibility.events.EntityMasterExtractionEvent;

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

    @Override
    public void load () throws IOException
    {
        MinecraftForge.EVENT_BUS.register (this);

        areEnemyBiomeOverridesEnabled = PvPMode.config.getBoolean ("Enable enemy biome override condition",
            LOTR_CONFIGURATION_CATEGORY, true,
            "If true, the PvP mode enemy biome override condition for LOTR biomes will be enabled. Players who are an enemy of a faction are forced to have PvP enabled while they're in a biome which is clearly assignable to that faction. This is highly configurable.");

        PvPMode.config.addCustomCategoryComment (LOTR_CONFIGURATION_CATEGORY,
            "Configuration entries for compatibility with the \"The Lord of the Rings Minecraft Mod\"");

        Path configurationFolder = PvPMode.config.getConfigFile ().getParentFile ().toPath ();

        FMLLog.info (String.format ("PvP mode overrides for LOTR biomes are %s",
            areEnemyBiomeOverridesEnabled ? "enabled" : "disabled"));
        if (areEnemyBiomeOverridesEnabled)
        {
            initEnemyBiomeOverrides (configurationFolder);
        }
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
            String line = null;
            for (int i = 1; (line = reader.readLine ()) != null; i++)
            {
                line = line.trim ();
                if (!line.isEmpty () && !line.startsWith ("#"))
                {
                    String[] parts = line.split (";");
                    if (parts.length != 3)
                    {
                        ++invalidEntryCounter;
                        FMLLog.warning (
                            "The lotr enemy biome config entry \"%s\" at line %d is invalid. There're too much or too less columns separated by semicolons!",
                            line,
                            i);
                    }
                    else
                    {
                        String faction = parts[0].trim ();
                        if (faction.equals ("ALL") || LOTRFaction.forName (faction) != null)
                        {
                            if (readValidFactionEntries.contains (faction))
                            {
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
                                    Integer alignmentInt = Integer.parseInt (alignmentString);

                                    String[] biomeIds = parts[2].trim ().split (",");
                                    if (biomeIds.length <= 0)
                                    {
                                        ++invalidEntryCounter;
                                        FMLLog.warning (
                                            "The lotr enemy biome config entry at line %d contains no assigned biome ids",
                                            i);
                                    }
                                    else
                                    {
                                        Collection<Integer> biomeIdsInt = new HashSet<> ();
                                        for (String biomeString : biomeIds)
                                        {
                                            String biomeStringTrimmed = biomeString.trim ();
                                            try
                                            {
                                                if (!biomeIdsInt.add (Integer.parseInt (biomeStringTrimmed)))
                                                {
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
                                            ++invalidEntryCounter;
                                            FMLLog.warning (
                                                "The lotr enemy biome config entry at line %d contains only invalid biome ids. The entry will be ignored.",
                                                i);
                                        }
                                        else
                                        {
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

}
