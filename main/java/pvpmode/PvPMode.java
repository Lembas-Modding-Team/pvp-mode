package pvpmode;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "pvp-mode", version = "1.6.0", acceptableRemoteVersions = "*")
public class PvPMode
{
    public static Configuration config;

    public static int roundFactor;
    public static int warmup;
    public static int cooldown;
    public static boolean radar;

    @EventHandler
    public void init (FMLInitializationEvent event)
    {
        PvPEventHandler.init ();
    }

    @EventHandler
    public void serverLoad (FMLServerStartingEvent event)
    {
        event.registerServerCommand (new PvPCommand ());
        event.registerServerCommand (new PvPListCommand ());
    }

    @EventHandler
    public void setupConfig (FMLPreInitializationEvent event)
    {
        config = new Configuration (event.getSuggestedConfigurationFile ());

        roundFactor = config.getInt ("Distance Rounding Factor", "MAIN", 64, 1, Integer.MAX_VALUE, "");
        warmup = config.getInt ("Warmup (seconds)", "MAIN", 300, 0, Integer.MAX_VALUE, "");
        cooldown = config.getInt ("Cooldown (seconds)", "MAIN", 900, 0, Integer.MAX_VALUE, "");
        radar = config.getBoolean ("Radar", "MAIN", true, "");

        if (config.hasChanged ())
            config.save ();
    }
}
