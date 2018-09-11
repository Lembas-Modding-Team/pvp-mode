package pvpmode.compatibility.modules.lootableBodies;

import pvpmode.compatibility.ForgeModCompatibilityModuleLoader;

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
    public String getCompatibilityModuleClassName ()
    {
        return "pvpmode.compatibility.modules.lootableBodies.LootableBodiesCompatibilityModule";
    }

}
