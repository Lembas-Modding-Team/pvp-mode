package pvpmode.command;

import java.util.*;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.*;
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
        return "/pvplist [all] OR /pvplist <maxEntryCount>";
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
        ArrayList<EntityPlayerMP> warmupPlayers = new ArrayList<> ();
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
                        int proximity = -1;
                        if (PvPMode.radar)
                        {
                            if (PvPMode.allowPerPlayerSpying
                                ? PvPUtils.getPvPData (senderPlayer).isSpyingEnabled ()
                                    && PvPUtils.getPvPData (player).isSpyingEnabled ()
                                : true)// If per player spying is enabled, both players need to have spying enabled
                            {
                                proximity = PvPUtils.roundedDistanceBetween (senderPlayer, player);
                            }
                        }

                        if (!unsafePlayers.containsKey (proximity))
                            unsafePlayers.put (proximity, new HashSet<> ());
                        unsafePlayers.get (proximity).add (player);
                        break;
                }
            }
        }

        PvPUtils.green (sender, "--- PvP Mode Player List ---");

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
            int entryCount = Math.min (maxDisplayedEntries == -1 ? unsafePlayers.size () : maxDisplayedEntries,
                playerCountWithoutSender);
            ChatComponentText entrySizeText = new ChatComponentText (String.format ("%d of %d players are displayed",
                entryCount,
                playerCountWithoutSender));
            entrySizeText.getChatStyle ().setColor (EnumChatFormatting.GRAY);
            entrySizeText.getChatStyle ().setItalic (true);
            sender.addChatMessage (entrySizeText);
        }
        PvPUtils.green (sender, "-------------------------");

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
                additionalComponent = new ChatComponentText ( (proximity != -1
                    && !isSenderPlayer && hasSenderPlayerPvPEnabled)
                        ? String.format (" - ~%d blocks", proximity)
                        : "");
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
        if (isSenderPlayer && PvPUtils.getPvPData (senderPlayer).isSpyingEnabled ())
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

}
