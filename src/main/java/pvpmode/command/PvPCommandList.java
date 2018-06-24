package pvpmode.command;

import java.util.*;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import pvpmode.*;

public class PvPCommandList extends AbstractPvPCommand
{
    @Override
    public String getCommandName ()
    {
        return "pvplist";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/pvplist";
    }

    @Override
    public boolean canCommandSenderUseCommand (ICommandSender sender)
    {
        return true;
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        EntityPlayerMP senderPlayer = getCommandSenderAsPlayer (sender);

        EnumPvPMode senderPlayerPvPMode = PvPUtils.getPvPMode (senderPlayer);

        ArrayList<EntityPlayerMP> safePlayers = new ArrayList<> ();
        ArrayList<EntityPlayerMP> warmupPlayers = new ArrayList<> ();
        TreeMap<Integer, Set<EntityPlayerMP>> unsafePlayers = new TreeMap<> ();

        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;
            EnumPvPMode mode = PvPUtils.getPvPMode (player);

            if (player != senderPlayer)
            {
                switch (mode)
                {
                    case OFF:
                        safePlayers.add (player);
                        break;
                    case ON:
                        int proximity = PvPMode.radar ? PvPUtils.roundedDistanceBetween (senderPlayer, player) : -1;
                        if (!unsafePlayers.containsKey (proximity))
                            unsafePlayers.put (proximity, new HashSet<> ());
                        unsafePlayers.get (proximity).add (player);
                        break;
                    case WARMUP:
                        warmupPlayers.add (player);
                        break;
                }
            }
        }

        PvPUtils.green (sender, "--- PvP Mode Player List ---");

        displayMessageForPlayer (senderPlayer, senderPlayer, senderPlayerPvPMode, senderPlayerPvPMode, -1);
        for (Integer distance : unsafePlayers.keySet ())
        {
            for (EntityPlayerMP player : unsafePlayers.get (distance))
            {
                displayMessageForPlayer (player, senderPlayer, EnumPvPMode.ON, senderPlayerPvPMode, distance);
            }
        }
        warmupPlayers.forEach (
            player -> displayMessageForPlayer (player, senderPlayer, EnumPvPMode.WARMUP, senderPlayerPvPMode, -1));
        safePlayers.forEach (
            player -> displayMessageForPlayer (player, senderPlayer, EnumPvPMode.OFF, senderPlayerPvPMode, -1));
        PvPUtils.green (sender, "-------------------------");

    }

    private void displayMessageForPlayer (EntityPlayerMP player, EntityPlayerMP senderPlayer, EnumPvPMode playerMode,
        EnumPvPMode senderPlayerMode,
        int proximity)
    {
        boolean isSenderPlayer = player == senderPlayer;
        boolean hasSenderPlayerPvPEnabled = senderPlayerMode == EnumPvPMode.ON;
        IChatComponent modeComponent = null;
        IChatComponent nameComponent = new ChatComponentText (String.format (" %s", player.getDisplayName ()));
        IChatComponent additionalComponent = null;
        switch (playerMode)
        {
            case OFF:
                modeComponent = new ChatComponentText (
                    String.format ("[OFF%s]", PvPUtils.isCreativeMode (player) ? ":GM1"
                        : PvPUtils.canFly (player) ? ":FLY" : ""));
                setComponentColors (EnumChatFormatting.GREEN, isSenderPlayer, modeComponent, nameComponent);
                break;
            case ON:
                modeComponent = new ChatComponentText ("[ON]");
                additionalComponent = new ChatComponentText ( (PvPMode.radar
                    && !isSenderPlayer && hasSenderPlayerPvPEnabled)
                        ? String.format (" - ~%d blocks", proximity)
                        : "");
                setComponentColors (EnumChatFormatting.RED, isSenderPlayer, modeComponent, nameComponent,
                    additionalComponent);
                break;
            case WARMUP:
                modeComponent = new ChatComponentText ("[WARMUP]");
                additionalComponent = new ChatComponentText (String.format (" - %d seconds till PvP",
                    PvPUtils.getPvPData (player).getPvPWarmup () - PvPUtils.getTime ()));
                setComponentColors (EnumChatFormatting.YELLOW, isSenderPlayer, modeComponent, nameComponent,
                    additionalComponent);
                break;
        }
        modeComponent.appendSibling (nameComponent);
        if (additionalComponent != null)
        {
            modeComponent.appendSibling (additionalComponent);
        }
        senderPlayer
            .addChatComponentMessage (modeComponent);
    }

    private void setComponentColors (EnumChatFormatting baseColor, boolean isSenderPlayer, IChatComponent... components)
    {
        boolean first = true;
        for (IChatComponent component : components)
        {
            component.getChatStyle ().setColor (!first && isSenderPlayer ? EnumChatFormatting.BLUE : baseColor);
            first = false;
        }
    }

}
