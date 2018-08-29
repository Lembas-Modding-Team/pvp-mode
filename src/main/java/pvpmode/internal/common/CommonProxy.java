package pvpmode.internal.common;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.compatibility.CompatibilityManager;
import pvpmode.api.common.configuration.CommonConfigurationConstants;
import pvpmode.internal.common.compatibility.CompatibilityManagerImpl;

public class CommonProxy
{

    protected Configuration configuration;
    protected CompatibilityManagerImpl compatibilityManager = new CompatibilityManagerImpl ();

    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        configuration = new Configuration (event.getSuggestedConfigurationFile ());

        configuration.addCustomCategoryComment (CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY,
            "General configuration entries");

        registerCompatibilityModules ();
    }

    protected void registerCompatibilityModules ()
    {
    }

    public void onInit (FMLInitializationEvent event) throws Exception
    {
        compatibilityManager.loadRegisteredModules ();
    }

    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {

    }

    public Configuration getConfiguration ()
    {
        return configuration;
    }

    public CompatibilityManager getCompatibilityManager ()
    {
        return compatibilityManager;
    }

}
