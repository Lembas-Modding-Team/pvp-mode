package pvpmode.internal.common.network;

import java.util.UUID;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import pvpmode.PvPMode;

public class PvPStateChangedMessage implements IMessage
{

    private UUID playerUUID;
    private boolean isInPvP;

    public PvPStateChangedMessage ()
    {
    }

    public PvPStateChangedMessage (UUID playerUUID, boolean isInPvP)
    {
        this.playerUUID = playerUUID;
        this.isInPvP = isInPvP;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        playerUUID = UUID.fromString (ByteBufUtils.readUTF8String (buf));
        isInPvP = buf.readBoolean ();
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String (buf, playerUUID.toString ());
        buf.writeBoolean (isInPvP);
    }

    public static class PvPStateChangedMessageHandler
        implements IMessageHandler<PvPStateChangedMessage, PvPStateChangedMessage>
    {

        @Override
        public PvPStateChangedMessage onMessage (PvPStateChangedMessage message, MessageContext ctx)
        {
            if (message.isInPvP)
            {
                PvPMode.instance.getClientProxy ().getPlayersInPvP ().add (message.playerUUID);
            }
            else
            {
                PvPMode.instance.getClientProxy ().getPlayersInPvP ().remove (message.playerUUID);
            }
            return null;
        }

    }

}
