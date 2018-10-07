package pvpmode.command;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.command.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import pvpmode.*;

public class SoulboundCommand extends AbstractPvPCommand
{

    @Override
    public String getCommandName ()
    {
        return "soulbound";
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return "/soulbound <player> [toggle|bind|unbind] [held|armor|main|hotbar|all]";
    }

    @Override
    public int getRequiredPermissionLevel ()
    {
        return 2;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages
            .add (Triple.of ("soulbound ", "<player> [toggle|bind|unbind] [held|armor|main|hotbar|all]",
                "Toggles the soulbound state."));
        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();
        messages
            .add (Triple.of ("soulbound ", "<player> [toggle|bind|unbind] [held|armor|main|hotbar|all]",
                "Toggles the soulbound state of the items in the specified inventory. If none was specified, the current held item will be used. Bind makes the item soulbound, unbind removes that property. If the new state is not specified, the old one will be toggled."));
        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "For operators. Allows them to mark a set of items of a player as soulbound, which means that it won't be dropped upon death. The player will be informed about that.";
    }

    @Override
    public void processCommand (ICommandSender admin, String[] args)
    {
        requireMinLength (admin, args, 1);

        EntityPlayerMP player = CommandBase.getPlayer (admin, args[0]);

        String action = args.length == 1 ? "toggle" : requireArguments (admin, args, 1, "toggle", "bind", "unbind");
        String inventory = args.length <= 2 ? "held"
            : requireArguments (admin, args, 2, "held", "armor", "main", "hotbar", "all");

        ItemStack[] stacks = getStacksInInventory (player, inventory);

        if (stacks == null || stacks.length <= 0)
            throw new CommandException (inventory.equals ("all") ? "That player has no item stacks in any inventory"
                : inventory.equals ("held") ? "That player isn't holding an item stack"
                    : "That player hasn't any items in the " + inventory + " inventory");

        switch (action)
        {
            case "bind":
                toggleSoulbound (admin, player, stacks, Boolean.TRUE);
                break;
            case "unbind":
                toggleSoulbound (admin, player, stacks, Boolean.FALSE);
                break;
            case "toggle":
                toggleSoulbound (admin, player, stacks, null);
                break;
        }
    }

    private void toggleSoulbound (ICommandSender sender, EntityPlayer player, ItemStack[] stacks, Boolean soulbound)
    {
        boolean isSingleStack = stacks.length == 1;
        int boundStacksCount = 0;
        int unboundStacksCount = 0;

        for (ItemStack stack : stacks)
        {
            boolean isStackSoulbound = PvPUtils.isSoulbound (stack);
            if (soulbound == null ? true : soulbound.booleanValue () != isStackSoulbound)
            {
                setSoulbound (stack, !isStackSoulbound);

                if (isSingleStack)
                {
                    if (sender != player)
                    {
                        ChatUtils.green (sender,
                            String.format ("%s's \"%s§r\" is %s soulbound", player.getDisplayName (),
                                stack.getDisplayName (),
                                !isStackSoulbound ? "now" : "no longer"));
                    }
                    else
                    {
                        ChatUtils.postLocalChatMessages (player,
                            !isStackSoulbound ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW,
                            String.format ("Your \"%s§r\" is %s soulbound", stack.getDisplayName (),
                                !isStackSoulbound ? "now" : "no longer"));
                    }
                }
                else
                {
                    if (!isStackSoulbound)
                    {
                        ++boundStacksCount;
                    }
                    else
                    {
                        ++unboundStacksCount;
                    }
                }
            }
            else
            {
                if (isSingleStack)
                {
                    ChatUtils.yellow (sender,
                        String.format ("%s's \"%s§r\" is %s", player.getDisplayName (), stack.getDisplayName (),
                            isStackSoulbound ? "already soulbound" : "not soulbound"));
                }
            }
        }

        if (!isSingleStack)
        {
            if (boundStacksCount > 0 || unboundStacksCount > 0)
            {
                if (boundStacksCount > 0)
                {
                    ChatUtils.green (sender,
                        String.format ("%d item stacks in %s's inventory are now soulbound", boundStacksCount,
                            player.getDisplayName ()));
                }
                if (unboundStacksCount > 0)
                {
                    ChatUtils.yellow (sender,
                        String.format ("%d item stacks in %s's inventory are no longer soulbound", unboundStacksCount,
                            player.getDisplayName ()));
                }
            }
            else
            {
                ChatUtils.yellow (sender,
                    String.format ("The soulbound state of no stacks in %s's inventory was toggled",
                        player.getDisplayName ()));
            }
        }
    }

    private void setSoulbound (ItemStack stack, boolean soulbound)
    {
        if (!stack.hasTagCompound ())
            stack.setTagCompound (new NBTTagCompound ());

        NBTTagCompound stackTag = stack.getTagCompound ();

        stackTag.setBoolean ("SoulboundBool", soulbound);

        String currentTooltip = stackTag.hasKey ("SoulboundTooltip") ? stackTag.getString ("SoulboundTooltip")
            : PvPMode.soulboundTooltip;

        NBTTagCompound displayTag = stackTag.getCompoundTag ("display");

        NBTTagList lore = displayTag.getTagList ("Lore", 8);

        if (soulbound)
        {
            lore.appendTag (new NBTTagString (PvPMode.soulboundTooltip));
            stackTag.setString ("SoulboundTooltip", PvPMode.soulboundTooltip);
        }
        else
        {
            for (int i = 0; i < lore.tagCount (); i++)
            {
                if (lore.getStringTagAt (i).equalsIgnoreCase (currentTooltip))
                {
                    lore.removeTag (i);
                }
            }
        }

        stackTag.setTag ("display", displayTag);

        displayTag.setTag ("Lore", (NBTBase) lore);
    }

    private ItemStack[] getStacksInInventory (EntityPlayer player, String inventory)// TODO: Outsource to utils
    {
        switch (inventory)
        {
            case "held":
                return removeEmptyStacks (player.getHeldItem ());
            case "armor":
                return removeEmptyStacks (player.inventory.armorInventory);
            case "hotbar":
                return removeEmptyStacks (Arrays.copyOfRange (player.inventory.mainInventory, 0, 8));
            case "main":
                return removeEmptyStacks (Arrays.copyOfRange (player.inventory.mainInventory, 9, 35));
            case "all":
                return removeEmptyStacks (
                    ArrayUtils.addAll (player.inventory.mainInventory, player.inventory.armorInventory));
            default:
                return null;
        }
    }

    private ItemStack[] removeEmptyStacks (ItemStack... inventory)// TODO: Outsource to utils
    {
        return inventory.length <= 0 ? null
            : Arrays.stream (inventory).filter ( (stack) -> stack != null).toArray (size -> new ItemStack[size]);
    }

    @Override
    public boolean isUsernameIndex (String[] args, int index)
    {
        return index == 0 && args.length > 0;
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord (args,
                MinecraftServer.getServer ().getAllUsernames ());
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord (args,
                "bind", "unbind", "toggle");
        if (args.length == 3)
            return CommandBase.getListOfStringsMatchingLastWord (args,
                "all", "hotbar", "armor", "main", "held");
        return null;
    }

}
