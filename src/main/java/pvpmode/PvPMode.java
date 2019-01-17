package pvpmode;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.*;
import cpw.mods.fml.common.event.*;
import pvpmode.api.common.version.SemanticVersion;
import pvpmode.internal.client.ClientProxy;
import pvpmode.internal.common.CommonProxy;
import pvpmode.internal.server.ServerProxy;

@Mod(modid = PvPMode.MODID, name = "@name@", version = PvPMode.VERSION, acceptableRemoteVersions = "*")
public class PvPMode
{

    public static final String MODID = "@modid@";
    public static final String VERSION = "@version@";
    public static final SemanticVersion SEMANTIC_VERSION = SemanticVersion.of (VERSION);

    @Instance
    public static PvPMode instance;

    @SidedProxy(clientSide = "pvpmode.internal.client.ClientProxy", serverSide = "pvpmode.internal.server.ServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) throws Exception
    {
        proxy.onPreInit (event);
    }

    @EventHandler
    public void onInit (FMLInitializationEvent event) throws Exception

    {
        proxy.onInit (event);
    }

    @EventHandler
    public void onPostInit (FMLPostInitializationEvent event) throws Exception

    {
        proxy.onPostInit (event);
    }

    @EventHandler
    public void onServerStarting (FMLServerStartingEvent event)
    {
        if (proxy instanceof ServerProxy)
        {
            getServerProxy ().onServerStarting (event);
        }
    }

    @EventHandler
    public void onLoadingComplete (FMLLoadCompleteEvent event)
    {
        proxy.onLoadingComplete (event);
    }

    @EventHandler
    public void onServerStopping (FMLServerStoppingEvent event)
    {
        if (proxy instanceof ServerProxy)
        {
            getServerProxy ().onServerStopping (event);
        }
    }

    public ClientProxy getClientProxy ()
    {
        return (ClientProxy) proxy;
    }

    public ServerProxy getServerProxy ()
    {
        return (ServerProxy) proxy;
    }
}