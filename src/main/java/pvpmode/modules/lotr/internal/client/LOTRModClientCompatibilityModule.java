package pvpmode.modules.lotr.internal.client;

import java.nio.file.Path;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.CompatibilityModuleLoader;
import pvpmode.modules.lotr.internal.client.gear.ClientBlockedGearManager;
import pvpmode.modules.lotr.internal.common.LOTRModCommonCompatibilityModule;

/**
 * The client-side compatibility module for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModClientCompatibilityModule extends LOTRModCommonCompatibilityModule
{

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        blockedGearManager = new ClientBlockedGearManager ();
        blockedGearManager.init (this);

        FMLCommonHandler.instance ().bus ().register (this);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut (ClientDisconnectionFromServerEvent event)
    {
        /*
         * This is important, so the gear blocking settings are reset upon leaving the
         * server, so they don't apply in singleplayer or on other servers.
         */
        ((ClientBlockedGearManager) blockedGearManager).setAreGearItemsBlockedServerside (false);
    }

    @Override
    public ClientBlockedGearManager getBlockedGearManager ()
    {
        return (ClientBlockedGearManager) blockedGearManager;
    }

}
