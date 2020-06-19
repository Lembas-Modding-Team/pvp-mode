package pvpmode.modules.lotr.internal.common;

import cpw.mods.fml.relauncher.Side;
import pvpmode.api.common.compatibility.ForgeModCompatibilityModuleLoader;
import pvpmode.modules.lotr.api.common.LOTRCommonConstants;

/**
 * The compatibility module loader for the LOTR Mod.
 *
 * @author CraftedMods
 *
 */
public class LOTRModCompatibilityModuleLoader extends ForgeModCompatibilityModuleLoader
{

    public LOTRModCompatibilityModuleLoader ()
    {
        super (LOTRCommonConstants.LOTR_MOD_MODID);
    }

    @Override
    public String getModuleName ()
    {
        return "LOTR Mod Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName (Side side)
    {
        return side == Side.SERVER
            ? "pvpmode.modules.lotr.internal.server.LOTRModServerCompatibilityModule"
            : "pvpmode.modules.lotr.internal.client.LOTRModClientCompatibilityModule";
    }

    @Override
    protected boolean isVersionSupported (String modid, String version)
    {
        return version.equals ("Update v36.2 for Minecraft 1.7.10");
    }

}
