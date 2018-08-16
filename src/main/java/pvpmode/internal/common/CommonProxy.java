package pvpmode.internal.common;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import pvpmode.api.common.configuration.CommonConfigurationConstants;
import pvpmode.internal.common.compatibility.CompatibilityManagerImpl;

public class CommonProxy
{

    public static Configuration configuration; // TODO: Only works because the client proxy doesn't use that
    public static CompatibilityManagerImpl compatibilityManager = new CompatibilityManagerImpl (); // TODO: Only works because the client proxy doesn't use that

    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        configuration = new Configuration (event.getSuggestedConfigurationFile ());

        configuration.addCustomCategoryComment (CommonConfigurationConstants.MAIN_CONFIGURATION_CATEGORY, "General configuration entries");

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

}
