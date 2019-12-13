package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.*;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.utils.*;

public class PvPCommandVault extends AbstractPvPCommand
{

    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVP_VAULT_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVP_VAULT_COMMAND_USAGE;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> ret = new ArrayList<> ();

        ret.add (Triple.of ("pvpvault info", "", "Displays the content"));
        ret.add (Triple.of ("pvpvault drop", "", "Drops the content"));

        return ret;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> ret = new ArrayList<> ();

        ret.add (Triple.of ("pvpvault info", "", "Displays the count if items stored in the vault."));
        ret.add (Triple.of ("pvpvault drop", "",
            "Drops the content of the vault to the <!§ground§!>, at the position where the player is currently standing."));
        return ret;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "The command can be used by a player to interact with their personal vault.";
    }

    @Override
    protected void processCommand (ICommandSender sender, String[] parsedArgs, String[] originalArgs)
    {
        EntityPlayer player = getCommandSenderAsPlayer (sender);
        PvPData pvpData = PvPServerUtils.getPvPData (player);
        List<ItemStack> vault = pvpData.getVault ();

        switch (requireArguments (sender, parsedArgs, 0, "drop", "info"))
        {
            case "drop":
                if (!vault.isEmpty ())
                {
                    for (ItemStack stack : vault)
                    {
                        player.entityDropItem (stack, 0.5f);
                    }
                    vault.clear ();
                    pvpData.setVault (vault);

                    ServerChatUtils.green (sender, "Dropped the contents of the vault to the ground");
                }
                else
                {
                    ServerChatUtils.yellow (sender, "Your vault is empty");
                }
                break;
            case "info":
                if (!vault.isEmpty ())
                {
                    ChatComponentText vaultInfo1 = new ChatComponentText ("Your vault contains ");
                    ChatComponentText vaultInfo2 = new ChatComponentText (Integer.toString (vault.size ()));
                    ChatComponentText vaultInfo3 = new ChatComponentText (" items. Click ");
                    ChatComponentText vaultInfo4 = new ChatComponentText ("HERE");
                    ChatComponentText vaultInfo5 = new ChatComponentText (" to drop them.");

                    vaultInfo1.getChatStyle ().setColor (EnumChatFormatting.GOLD);
                    vaultInfo2.getChatStyle ().setColor (EnumChatFormatting.GREEN);
                    vaultInfo3.getChatStyle ().setColor (EnumChatFormatting.GOLD);

                    vaultInfo4.getChatStyle ().setColor (EnumChatFormatting.YELLOW);
                    vaultInfo4.getChatStyle ().setUnderlined (true);
                    vaultInfo4.getChatStyle ().setChatHoverEvent (new HoverEvent (Action.SHOW_TEXT,
                        new ChatComponentText ("Click to drop the contents of the vault")));
                    vaultInfo4.getChatStyle ().setChatClickEvent (new ClickEvent (ClickEvent.Action.RUN_COMMAND,
                        "/pvpvault drop"));

                    vaultInfo5.getChatStyle ().setColor (EnumChatFormatting.GOLD);

                    sender.addChatMessage (vaultInfo1.appendSibling (vaultInfo2).appendSibling (vaultInfo3)
                        .appendSibling (vaultInfo4).appendSibling (vaultInfo5));
                }
                else
                {
                    ServerChatUtils.yellow (sender, "Your vault is empty");
                }

                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getTabCompletionOptions (ICommandSender sender, String[] args)
    {
        return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord (args, "info", "drop") : null;
    }

}
