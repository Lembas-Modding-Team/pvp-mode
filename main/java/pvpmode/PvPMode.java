package pvpmode;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "pvp-mode", version = "1.5.0", acceptableRemoteVersions = "*")
public class PvPMode
{
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
}
