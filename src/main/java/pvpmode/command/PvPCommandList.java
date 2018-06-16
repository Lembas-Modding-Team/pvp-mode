package pvpmode.command;

import java.util.ArrayList;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import pvpmode.*;

public class PvPCommandList extends CommandBase
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

        String senderPlayerMessage = PvPUtils.SOMETHING_WENT_WRONG_MESSAGE;
        ArrayList<String> safePlayers = new ArrayList<String> ();
        ArrayList<String> warmupPlayers = new ArrayList<String> ();
        ArrayList<String> unsafePlayers = new ArrayList<String> ();

        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;
            EnumPvPMode mode = PvPUtils.getPvPMode (player);

            if (player == senderPlayer)// One time this must be true
            {
                senderPlayerMessage = getMessageForPlayer (player, senderPlayer, mode) + " (You)";
            }
            else
            {
                switch (mode)
                {
                    case OFF:
                        safePlayers.add (getMessageForPlayer (player, senderPlayer, mode));
                        break;
                    case ON:
                        unsafePlayers.add (getMessageForPlayer (player, senderPlayer, mode));
                        break;
                    case WARMUP:
                        warmupPlayers.add (getMessageForPlayer (player, senderPlayer, mode));
                        break;
                }
            }
        }

        PvPUtils.blue (sender, senderPlayerMessage);
        for (String line : unsafePlayers)
            PvPUtils.red (sender, line);
        for (String line : warmupPlayers)
            PvPUtils.yellow (sender, line);
        for (String line : safePlayers)
            PvPUtils.green (sender, line);
    }

    private String getMessageForPlayer (EntityPlayerMP player, EntityPlayerMP senderPlayer, EnumPvPMode mode)
    {
        switch (mode)
        {
            case OFF:
                return String.format ("[OFF%s] %s", PvPUtils.isCreativeMode (player) ? ":GM1"
                    : PvPUtils.canFly (player) ? ":FLY" : "", player.getDisplayName ());
            case ON:
                return String.format ("[ON] %s %s", player.getDisplayName (), ( (PvPMode.radar
                    && senderPlayer != player)
                        ? String.format ("- ~%d blocks",
                            PvPUtils.roundedDistanceBetween (senderPlayer, player))
                        : ""));
            case WARMUP:
                return String.format ("[WARMUP] %s - %d seconds till PvP", player.getDisplayName (),
                    (PvPUtils.getPvPData (player).getPvPWarmup () - PvPUtils.getTime ()));
        }
        return PvPUtils.SOMETHING_WENT_WRONG_MESSAGE;// Shouldn't happen
    }

}
