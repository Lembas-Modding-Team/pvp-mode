package pvpmode.command;

import java.util.*;

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
        TreeMap<Integer, Set<String>> unsafePlayers = new TreeMap<> ();

        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;
            EnumPvPMode mode = PvPUtils.getPvPMode (player);

            if (player == senderPlayer)// One time this must be true
            {
                senderPlayerMessage = getMessageForPlayer (player, senderPlayer, mode, -1) + " (You)";
            }
            else
            {
                switch (mode)
                {
                    case OFF:
                        safePlayers.add (getMessageForPlayer (player, senderPlayer, mode, -1));
                        break;
                    case ON:
                        int proximity = PvPMode.radar ? PvPUtils.roundedDistanceBetween (senderPlayer, player) : -1;
                        if (!unsafePlayers.containsKey (proximity))
                            unsafePlayers.put (proximity, new HashSet<> ());
                        unsafePlayers.get (proximity).add (getMessageForPlayer (player, senderPlayer, mode, proximity));
                        break;
                    case WARMUP:
                        warmupPlayers.add (getMessageForPlayer (player, senderPlayer, mode,
                            -1));
                        break;
                }
            }
        }

        PvPUtils.green (sender, "--- PvP Mode Player List ---");
        PvPUtils.blue (sender, senderPlayerMessage);
        for (Set<String> lines : unsafePlayers.values ())
        {
            for (String line : lines)
                PvPUtils.red (sender, line);
        }
        for (String line : warmupPlayers)
            PvPUtils.yellow (sender, line);
        for (String line : safePlayers)
            PvPUtils.green (sender, line);
        PvPUtils.green (sender, "-------------------------");
    }

    private String getMessageForPlayer (EntityPlayerMP player, EntityPlayerMP senderPlayer, EnumPvPMode mode,
        int proximity)
    {
        switch (mode)
        {
            case OFF:
                return String.format ("[OFF%s] %s", PvPUtils.isCreativeMode (player) ? ":GM1"
                    : PvPUtils.canFly (player) ? ":FLY" : "", player.getDisplayName ());
            case ON:
                return String.format ("[ON] %s %s", player.getDisplayName (), ( (PvPMode.radar
                    && senderPlayer != player)
                        ? String.format ("- ~%d blocks", proximity)
                        : ""));
            case WARMUP:
                return String.format ("[WARMUP] %s - %d seconds till PvP", player.getDisplayName (),
                    (PvPUtils.getPvPData (player).getPvPWarmup () - PvPUtils.getTime ()));
        }
        return PvPUtils.SOMETHING_WENT_WRONG_MESSAGE;// Shouldn't happen
    }

}
