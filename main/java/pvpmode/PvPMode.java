package pvpmode;

import java.io.IOException;
import java.util.Arrays;

import cpw.mods.fml.common.*;
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
import pvpmode.log.*;

@Mod(modid = "pvp-mode", name = "PvP Mode", version = "1.0", acceptableRemoteVersions = "*")
public class PvPMode
{
    public static Configuration config;
    public static ServerConfigurationManager cfg;

    public static int roundFactor;
    public static int warmup;
    public static int cooldown;
    public static boolean radar;
    public static String pvpLoggingHandler;

    @EventHandler
    public void preinit(FMLPreInitializationEvent event) throws IOException
    {
        config = new Configuration (event.getSuggestedConfigurationFile ());

        roundFactor = config.getInt ("Distance Rounding Factor", "MAIN", 64, 1, Integer.MAX_VALUE, "");
        warmup = config.getInt ("Warmup (seconds)", "MAIN", 300, 0, Integer.MAX_VALUE, "");
        cooldown = config.getInt ("Cooldown (seconds)", "MAIN", 900, 0, Integer.MAX_VALUE, "");
        radar = config.getBoolean ("Radar", "MAIN", true, "");
        pvpLoggingHandler = config.getString ("Pvp Logging Handler", "MAIN", PvPCombatLog.getDefaultHandlerName (),
                        "Valid values: " + Arrays.toString (PvPCombatLog.getValidHandlerNames ()),
                        PvPCombatLog.getValidHandlerNames ()).trim ();

        if (!PvPCombatLog.isValidHandlerName (pvpLoggingHandler))
        {
            FMLLog.warning ("The pvp combat logging handler \"%s\" is not valid. The default one will be used.",
                            pvpLoggingHandler);
            pvpLoggingHandler = PvPCombatLog.getDefaultHandlerName ();
        }else if(pvpLoggingHandler.equals (PvPCombatLog.NO_HANDLER_NAME)) {
            FMLLog.info ("Pvp combat logging is disabled");
        }

        if (config.hasChanged ())
            config.save ();

        PvPCombatLog.init (event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        PvPEventHandler.init ();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        cfg = MinecraftServer.getServer ().getConfigurationManager ();
        event.registerServerCommand (new PvPCommand ());
        event.registerServerCommand (new PvPCommandList ());
        event.registerServerCommand (new PvPCommandAdmin ());
        event.registerServerCommand (new PvPCommandCancel ());
        event.registerServerCommand (new PvPCommandHelp ());
    }

    @EventHandler
    public void serverClose(FMLServerStoppingEvent event)
    {
        PvPCombatLog.close ();
    }
}
