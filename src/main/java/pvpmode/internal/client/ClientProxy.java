package pvpmode.internal.client;

import java.util.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.client.configuration.ClientConfiguration;
import pvpmode.api.common.configuration.*;
import pvpmode.internal.client.configuration.ClientConfigurationImpl;
import pvpmode.internal.common.CommonProxy;

public class ClientProxy extends CommonProxy
{

    private PvPClientEventHandler eventHandler;

    @Override
    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        super.onPreInit (event);

        Map<String, ConfigurationPropertyKey<?>> properties = autoConfigManager.getGeneratedKeys ().getOrDefault (
            ClientConfiguration.CLIENT_CONFIG_PID,
            new HashMap<> ());
        properties.putAll (autoConfigManager.getGeneratedKeys ().getOrDefault (CommonConfiguration.COMMON_CONFIG_PID,
            new HashMap<> ()));
        // TODO: As long as we don't have clientside configs, getOrDefault has to be
        // used

        configuration = new ClientConfigurationImpl (this, forgeConfiguration,
            properties);
        configuration.load ();

        eventHandler = new PvPClientEventHandler ();

        MinecraftForge.EVENT_BUS.register (eventHandler);
        FMLCommonHandler.instance ().bus ().register (eventHandler);
    }

    @Override
    public void onInit (FMLInitializationEvent event) throws Exception
    {
        super.onInit (event);
    }

    @Override
    public void onPostInit (FMLPostInitializationEvent event) throws Exception
    {
        super.onPostInit (event);
    }

}
