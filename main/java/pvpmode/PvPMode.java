package pvpmode;

import java.io.IOException;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.common.config.Configuration;
import pvpmode.command.PvPCommand;
import pvpmode.command.PvPCommandAdmin;
import pvpmode.command.PvPCommandCancel;
import pvpmode.command.PvPCommandHelp;
import pvpmode.command.PvPCommandList;

@Mod(modid = "pvp-mode", name = "PvP Mode", version = "1.0", acceptableRemoteVersions = "*")
public class PvPMode
{
    public static Configuration config;
    public static ServerConfigurationManager cfg;

    public static int roundFactor;
    public static int warmup;
    public static int cooldown;
    public static boolean radar;
    public static boolean cancelTeleportation;
    public static int teleportationThreshold;

    @EventHandler
    public void preinit (FMLPreInitializationEvent event) throws IOException
    {
        config = new Configuration (event.getSuggestedConfigurationFile ());

        roundFactor = config.getInt ("Distance Rounding Factor", "MAIN", 64, 1, Integer.MAX_VALUE, "");
        warmup = config.getInt ("Warmup (seconds)", "MAIN", 300, 0, Integer.MAX_VALUE, "");
        cooldown = config.getInt ("Cooldown (seconds)", "MAIN", 900, 0, Integer.MAX_VALUE, "");
        radar = config.getBoolean ("Radar", "MAIN", true, "");
        cancelTeleportation = config.getBoolean ("Cancel Teleportation", "MAIN", true, "Whether or not to block teleportation");
        teleportationThreshold = config.getInt ("Teleportation Threshold", "MAIN", 10, "The maximum amount of blocks a player can move in one update event. Might need to be increased if your server is laggy or people aren't able to move fast.");

        if (config.hasChanged ())
            config.save ();

        PvPCombatLog.init (event);
    }

    @EventHandler
    public void init (FMLInitializationEvent event)
    {
        PvPEventHandler.init ();
    }

    @EventHandler
    public void serverLoad (FMLServerStartingEvent event)
    {
        cfg = MinecraftServer.getServer ().getConfigurationManager ();
        event.registerServerCommand (new PvPCommand ());
        event.registerServerCommand (new PvPCommandList ());
        event.registerServerCommand (new PvPCommandAdmin ());
        event.registerServerCommand (new PvPCommandCancel ());
        event.registerServerCommand (new PvPCommandHelp ());
    }

    @EventHandler
    public void serverClose (FMLServerStoppingEvent event)
    {
        PvPCombatLog.close ();
    }
}
