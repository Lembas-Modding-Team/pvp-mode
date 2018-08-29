package pvpmode.internal.common.core;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.*;

@MCVersion(value = "1.7.10")
@SortingIndex(value = 5000)
@TransformerExclusions(value = "pvpmode.internal.common.core")
public class PvPModeCore implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass ()
    {
        return new String[]
        {PvPModeClassTransformer.class.getName ()};
    }

    @Override
    public String getModContainerClass ()
    {
        return null;
    }

    @Override
    public String getSetupClass ()
    {
        return null;
    }

    @Override
    public void injectData (Map<String, Object> data)
    {

    }

    @Override
    public String getAccessTransformerClass ()
    {
        return null;
    }

}
