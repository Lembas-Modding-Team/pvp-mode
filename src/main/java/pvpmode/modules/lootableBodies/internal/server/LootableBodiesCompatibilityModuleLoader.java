package pvpmode.modules.lootableBodies.internal.server;

import cpw.mods.fml.relauncher.Side;
import pvpmode.api.common.compatibility.ForgeModCompatibilityModuleLoader;

public class LootableBodiesCompatibilityModuleLoader extends ForgeModCompatibilityModuleLoader
{

    public LootableBodiesCompatibilityModuleLoader ()
    {
        super ("lootablebodies");
    }

    @Override
    public String getModuleName ()
    {
        return "Lootable Bodies Compatibility";
    }

    @Override
    public String getCompatibilityModuleClassName (Side side)
    {
        return "pvpmode.modules.lootableBodies.internal.server.LootableBodiesCompatibilityModule";
    }

    @Override
    protected boolean isVersionSupported (String modid, String version)
    {
        return version.equals ("1.3.6");
    }

}
