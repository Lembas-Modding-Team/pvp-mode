package pvpmode.internal.common.network;

import java.util.Arrays;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import pvpmode.PvPMode;
import pvpmode.api.server.network.*;
import pvpmode.internal.server.network.ClientDataImpl;

/**
 * A package that is sent when the client requests clientside support from the
 * server. That means, that the server will send relevant data and game events
 * to the client, so that it can process them. To example the client can be
 * notified about PvP Mode changes, running timers, and so on.
 * 
 * @author CraftedMods
 *
 */
public class ClientsideFeatureSupportRequest implements IMessage
{

    /**
     * The version of the PvP Mode Mod the client uses. Has to be a semantic version
     * string.
     */
    private String pvpModeVersion;
    private String[] loadedCompatibilityModules;

    public ClientsideFeatureSupportRequest ()
    {
    }

    public ClientsideFeatureSupportRequest (String pvpModeVersion, String[] loadedComptibilityModules)
    {
        this.pvpModeVersion = pvpModeVersion;
        this.loadedCompatibilityModules = loadedComptibilityModules;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        pvpModeVersion = ByteBufUtils.readUTF8String (buf);
        loadedCompatibilityModules = new String[buf.readInt ()];
        for (int i = 0; i < loadedCompatibilityModules.length; i++)
        {
            loadedCompatibilityModules[i] = ByteBufUtils.readUTF8String (buf);
        }
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String (buf, pvpModeVersion);
        buf.writeInt (loadedCompatibilityModules.length);
        for (String loadedCompatibilityModule : loadedCompatibilityModules)
        {
            ByteBufUtils.writeUTF8String (buf, loadedCompatibilityModule);
        }
    }

    /**
     * The answer the server sends to the client after the request. Tells the client
     * whether it'll be supported.
     * 
     * @author CraftedMods
     *
     */
    public static class ClientsideFeatureSupportRequestAnswer implements IMessage
    {

        /**
         * Whether the client will be supported
         */
        private boolean supported;

        public ClientsideFeatureSupportRequestAnswer ()
        {
        }

        public ClientsideFeatureSupportRequestAnswer (boolean supported)
        {
            this.supported = supported;
        }

        @Override
        public void fromBytes (ByteBuf buf)
        {
            supported = buf.readBoolean ();
        }

        @Override
        public void toBytes (ByteBuf buf)
        {
            buf.writeBoolean (supported);
        }

    }

    public static class ClientsideFeatureSupportRequestHandler
        implements IMessageHandler<ClientsideFeatureSupportRequest, ClientsideFeatureSupportRequestAnswer>
    {

        @Override
        public ClientsideFeatureSupportRequestAnswer onMessage (ClientsideFeatureSupportRequest message,
            MessageContext ctx)
        {
            if (!MinecraftServer.getServer ().isSinglePlayer ())
            {
                EntityPlayerMP player = ctx.getServerHandler ().playerEntity;

                ClientData clientData = new ClientDataImpl (player, message.pvpModeVersion,
                    Arrays.asList (message.loadedCompatibilityModules));

                ClientsideSupportHandler supportHandler = PvPMode.instance.getServerProxy ()
                    .getClientsideSupportHandler ();

                if (!supportHandler.isClientSupported (player)) // If the client does not have client-side support yet
                {
                    if (supportHandler.addSupportedClient (clientData))
                    {
                        PvPMode.proxy.getLogger ().debug (
                            "The client %s requested client-side support and got it", clientData);
                        supportHandler.sendInitialSupportPackages (player);

                        return new ClientsideFeatureSupportRequestAnswer (true);
                    }
                    else
                    {
                        PvPMode.proxy.getLogger ().debug (
                            "The client %s requested client-side support, but could not be supported", clientData);
                        return new ClientsideFeatureSupportRequestAnswer (false);
                    }
                }
            }
            return null;
        }

    }

    public static class ClientsideFeatureSupportRequestAnswerHandler
        implements IMessageHandler<ClientsideFeatureSupportRequestAnswer, ClientsideFeatureSupportRequestAnswer>
    {

        @Override
        public ClientsideFeatureSupportRequestAnswer onMessage (ClientsideFeatureSupportRequestAnswer message,
            MessageContext ctx)
        {
            if (message.supported)
            {
                PvPMode.proxy.getLogger ().info (
                    "The server supports the client - client-side support will be enabled");
            }
            else
            {
                PvPMode.proxy.getLogger ().warning (
                    "The server doesn't support the client - client-side support will be disabled");
            }
            return null;
        }

    }

}
