package pvpmode.modules.lotr.internal.server.core;

import net.minecraft.entity.player.EntityPlayer;
import pvpmode.PvPMode;
import pvpmode.api.common.EnumPvPMode;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.internal.server.ServerProxy;
import pvpmode.modules.lotr.internal.common.LOTRModCompatibilityModuleLoader;
import pvpmode.modules.lotr.internal.server.LOTRModServerCompatibilityModule;

public class LOTRModServerSideHooks
{
    /*
     * Defaults to false (for the default LOTR behavior), unless conditions apply.
     */
    public static boolean getMapLocationVisibility (EntityPlayer seeingPlayer, EntityPlayer playerToBeSeen)
    {
        ServerProxy server = PvPMode.instance.getServerProxy ();
        LOTRModCompatibilityModuleLoader loader = server.getCompatibilityManager ()
            .getCompatibilityModuleLoaderInstance (LOTRModCompatibilityModuleLoader.class);
        LOTRModServerCompatibilityModule module = (LOTRModServerCompatibilityModule) server.getCompatibilityManager ()
            .getLoadedModules ().get (loader);

        if (module != null)
        {

            /*
             * Intelligence needs to be enabled, and the synchronization with the map
             * location too, and the player eventually to be seen needs to have PvP enabled.
             * 
             * If per player spying settings are allowed, and the player to be seen has
             * spying enabled, then the seeing player needs to have PvP and spying enabled
             * too.
             */

            if (server.getConfiguration ().isIntelligenceEnabled ()
                && module.getConfiguration ().isMapLocationSynchronizedWithIntelligence ()
                && PvPServerUtils.getPvPMode (playerToBeSeen) == EnumPvPMode.ON
                && PvPServerUtils.getPvPMode (seeingPlayer) == EnumPvPMode.ON
                && (!server.getConfiguration ().arePerPlayerSpyingSettingsAllowed () ||
                    PvPServerUtils.getPvPData (playerToBeSeen).isSpyingEnabled ()
                        && PvPServerUtils.getPvPData (seeingPlayer).isSpyingEnabled ()))
                return true;
        }

        return false;
    }

}
