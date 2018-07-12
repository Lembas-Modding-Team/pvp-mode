package pvpmode.command;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.*;
import pvpmode.compatibility.events.PvPListEvent;

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
        return "/pvplist [all] OR /pvplist <maxEntryCount>";
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvplist", "", "Displays information about all players with PvP on."));
        messages.add (Triple.of ("pvplist ", "<count>", "Displays information about 'count' players."));
        messages.add (Triple.of ("pvplist all", "", "Displays information about all players."));
        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages.add (Triple.of ("pvplist", "", "Displays the information of all players with PvP enabled."));
        messages.add (Triple.of ("pvplist ", "<count>",
            "Displays the information for as much players as specified, starting with players who have PvP enabled."));
        messages
            .add (Triple.of ("pvplist all", "", "Displays the information for all players on the server."));
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "Gives you information about the PvP modes, warmup timers, and eventual spy info (proximity and direction) of the players on the server. These are sorted. Spy information can only be accessed if you have PvP enabled, and eventual spying (depending on the server configuration). If the server allows custom spy settings, you'll only retrieve spy information about players who have spying enabled. \nYou'll always be displayed on the top of the list.";
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

        int maxDisplayedEntries = -1; // -1 means that only players with PvP on will be displayed

        if (args.length > 0)
        {
            String arg = args[0];
            if (arg.equals ("all"))
            {
                maxDisplayedEntries = Integer.MAX_VALUE;
            }
            else
            {
                maxDisplayedEntries = parseIntWithMin (sender, arg, 1);
            }
        }

        EnumPvPMode senderPlayerPvPMode = PvPUtils.getPvPMode (senderPlayer);

        ArrayList<EntityPlayerMP> safePlayers = new ArrayList<> ();
        TreeSet<EntityPlayerMP> warmupPlayers = new TreeSet<> ( (p1, p2) ->
        {
            return Long.compare (PvPUtils.getWarmupTimer (p1), PvPUtils.getWarmupTimer (p2));
        });
        ArrayList<EntityPlayerMP> unsafePlayersLowPriority = new ArrayList<> ();
        TreeMap<Integer, Set<EntityPlayerMP>> unsafePlayers = new TreeMap<> ( (c1, c2) ->
        {
            // A comparator which weights negative values as very large
            if (c1 < 0 || c2 < 0)
            {
                if (c1 < 0 && c2 >= 0)
                    return Integer.MAX_VALUE;
                if (c1 < 0 && c2 < 0)
                    return 0;
                if (c1 >= 0 && c2 < 0)
                    return Integer.MIN_VALUE;
            }
            return c1.compareTo (c2);
        });

        /*
         * All players will be processed regardless of the display size. Only then the
         * command can determine which entries are the most relevant for the calling
         * player and have to be displayed on the top of the list.
         */
        for (Object o : PvPMode.cfg.playerEntityList)
        {
            EntityPlayerMP player = (EntityPlayerMP) o;
            EnumPvPMode mode = PvPUtils.getPvPMode (player);

            if (player != senderPlayer)
            {
                switch (mode)
                {
                    case OFF:
                        if (PvPUtils.isWarmupTimerRunning (player))
                        {
                            warmupPlayers.add (player);
                        }
                        else
                        {
                            safePlayers.add (player);
                        }
                        break;
                    case ON:
                        if (!MinecraftForge.EVENT_BUS
                            .post (new PvPListEvent.UnsafeClassification (senderPlayer, player)))
                        {
                            // The player is unsafe with a high priority
                            int proximity = -1;
                            if (PvPMode.radar)
                            {
                                if (PvPMode.allowPerPlayerSpying
                                    ? PvPUtils.getPvPData (senderPlayer).isSpyingEnabled ()
                                        && PvPUtils.getPvPData (player).isSpyingEnabled ()
                                    : true)// If per player spying is enabled, both players need to have spying enabled
                                {
                                    if (!MinecraftForge.EVENT_BUS
                                        .post (new PvPListEvent.ProximityVisibility (senderPlayer, player)))
                                    {
                                        proximity = PvPUtils.roundedDistanceBetween (senderPlayer, player);
                                    }
                                }
                            }
                            if (!unsafePlayers.containsKey (proximity))
                                unsafePlayers.put (proximity, new HashSet<> ());
                            unsafePlayers.get (proximity).add (player);
                        }
                        else
                        {
                            // The player is unsafe with a low priority
                            unsafePlayersLowPriority.add (player);
                        }
                        break;
                }
            }
        }

        ChatUtils.green (sender, "--- PvP Mode Player List ---");

        displayMessageForPlayer (senderPlayer, senderPlayer, senderPlayerPvPMode, senderPlayerPvPMode, -1);

        int displayedEntries = 0;
        for (Integer distance : unsafePlayers.keySet ())
        {
            for (EntityPlayerMP player : unsafePlayers.get (distance))
            {
                if (maxDisplayedEntries == -1 || displayedEntries < maxDisplayedEntries)
                {
                    displayMessageForPlayer (player, senderPlayer, EnumPvPMode.ON, senderPlayerPvPMode, distance);
                    ++displayedEntries;
                }
            }
        }
        if (maxDisplayedEntries != -1)
        {
            for (EntityPlayerMP player : unsafePlayersLowPriority)
            {
                if (displayedEntries < maxDisplayedEntries)
                {
                    displayMessageForPlayer (player, senderPlayer, EnumPvPMode.ON, senderPlayerPvPMode, -1);
                    ++displayedEntries;
                }
            }
            for (EntityPlayerMP player : warmupPlayers)
            {
                if (displayedEntries < maxDisplayedEntries)
                {
                    displayMessageForPlayer (player, senderPlayer, EnumPvPMode.OFF, senderPlayerPvPMode, -1);
                    ++displayedEntries;
                }
            }
            for (EntityPlayerMP player : safePlayers)
            {
                if (displayedEntries < maxDisplayedEntries)
                {
                    displayMessageForPlayer (player, senderPlayer, EnumPvPMode.OFF, senderPlayerPvPMode, -1);
                    ++displayedEntries;
                }
            }
        }

        int playerCountWithoutSender = PvPMode.cfg.playerEntityList.size () - 1;
        if (playerCountWithoutSender != 0)
        {
            int unsafePlayersCount = (int) unsafePlayers.values ().parallelStream ()
                .collect (Collectors.summarizingInt (set -> set.size ())).getSum ();
            int entryCount = Math.min (maxDisplayedEntries == -1 ? unsafePlayersCount : maxDisplayedEntries,
                playerCountWithoutSender);

            ChatComponentText entrySizeText = new ChatComponentText (String.format ("%d of %d players are displayed",
                entryCount,
                playerCountWithoutSender));
            entrySizeText.getChatStyle ().setColor (EnumChatFormatting.GRAY);
            entrySizeText.getChatStyle ().setItalic (true);
            sender.addChatMessage (entrySizeText);
        }
        ChatUtils.green (sender, "-------------------------");

    }

    private void displayMessageForPlayer (EntityPlayerMP player, EntityPlayerMP senderPlayer, EnumPvPMode playerMode,
        EnumPvPMode senderPlayerMode,
        int proximity)
    {
        boolean isSenderPlayer = player == senderPlayer;
        boolean hasSenderPlayerPvPEnabled = senderPlayerMode == EnumPvPMode.ON;
        boolean isWarmupTimerRunning = PvPUtils.isWarmupTimerRunning (player);
        IChatComponent modeComponent = null;
        IChatComponent spyComponent = new ChatComponentText (" [SPY]");
        IChatComponent nameComponent = new ChatComponentText (String.format (" %s", player.getDisplayName ()));
        IChatComponent additionalComponent = null;
        switch (playerMode)
        {
            case OFF:
                modeComponent = new ChatComponentText (
                    String.format ("[OFF%s]", PvPUtils.isCreativeMode (player) ? ":GM1"
                        : PvPUtils.canFly (player) ? ":FLY" : ""));
                setComponentColors (isWarmupTimerRunning ? EnumChatFormatting.YELLOW : EnumChatFormatting.GREEN,
                    isSenderPlayer, modeComponent,
                    nameComponent, spyComponent);
                break;
            case ON:
                modeComponent = new ChatComponentText ("[ON]");
                if (proximity != -1
                    && !isSenderPlayer && hasSenderPlayerPvPEnabled)
                {
                    String proximityDirection = PvPMode.showProximityDirection
                        ? PvPUtils.getPlayerDirection (senderPlayer, player)
                        : "";
                    additionalComponent = new ChatComponentText (
                        String.format (" - ~%d blocks %s", proximity, proximityDirection));
                }
                setComponentColors (EnumChatFormatting.RED, isSenderPlayer, modeComponent, nameComponent,
                    additionalComponent, spyComponent);
                break;
        }
        if (isWarmupTimerRunning)
        {
            ChatComponentText warmupComponent = new ChatComponentText (
                String.format (" [%ds]", PvPUtils.getWarmupTimer (player)));
            warmupComponent.getChatStyle ().setColor (EnumChatFormatting.YELLOW);
            modeComponent
                .appendSibling (warmupComponent);
        }
        if (isSenderPlayer && PvPMode.radar && PvPMode.allowPerPlayerSpying
            && PvPUtils.getPvPData (senderPlayer).isSpyingEnabled ())
        {
            modeComponent.appendSibling (spyComponent);
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
            if (component != null)
            {
                component.getChatStyle ().setColor (!first && isSenderPlayer ? EnumChatFormatting.BLUE : baseColor);
                first = false;
            }
        }
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args, "all");
        return null;
    }

}
