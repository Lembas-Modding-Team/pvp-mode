package pvpmode.internal.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.PvPMode;
import pvpmode.api.server.network.ClientsideSupportHandler;

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

    public ClientsideFeatureSupportRequest ()
    {
    }

    public ClientsideFeatureSupportRequest (String pvpModeVersion)
    {
        this.pvpModeVersion = pvpModeVersion;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        pvpModeVersion = ByteBufUtils.readUTF8String (buf);
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String (buf, pvpModeVersion);
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
            EntityPlayerMP player = ctx.getServerHandler ().playerEntity;
            UUID playerUUID = player.getUniqueID ();
            ClientsideSupportHandler supportHandler = PvPMode.instance.getServerProxy ().getClientsideSupportHandler ();

            if (!supportHandler.isClientsideSupported (playerUUID))
            {
                if (supportHandler.isRemoteVersionSupported (message.pvpModeVersion))
                {
                    PvPMode.proxy.getLogger ().info (
                        "The client of %s requested client-side support and got it - the client's PvP Mode Version (%s) is supported",
                        player.getDisplayName (), message.pvpModeVersion);
                    supportHandler.addClientsideSupport (playerUUID);
                    return new ClientsideFeatureSupportRequestAnswer (true);
                }
                {
                    PvPMode.proxy.getLogger ().info (
                        "The client of %s requested client-side support, but the client's PvP Mode Version (%s) is not supported",
                        player.getDisplayName (), message.pvpModeVersion);
                    return new ClientsideFeatureSupportRequestAnswer (false);
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
                    "The server supports the local PvP Mode version - clientside support will be enabled");
            }
            else
            {
                PvPMode.proxy.getLogger ().warning (
                    "The server doesn't support the local PvP Mode version - clientside support is disabled");
            }
            return null;
        }

    }

}
