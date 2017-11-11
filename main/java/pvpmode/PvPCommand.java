package pvpmode;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PvPCommand extends CommandBase
{
	ChatComponentText badperm = new ChatComponentText (EnumChatFormatting.RED
		+ "You do not have permission to toggle another player's PvP mode!");
	ChatComponentText help = new ChatComponentText (EnumChatFormatting.RED
		+ "/pvp [player]");
	
	@Override
	public String getCommandName()
	{
		return "pvp";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return help.getUnformattedText();
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		EntityPlayerMP target;
		ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();
		long cooldown = 0;
		
		if (args.length == 0)
		{
			target = getCommandSenderAsPlayer (sender);
			cooldown = 5000;
		}
		//As in "/pvp VulcanForge"
		else if (args.length == 1)
		{
			//152596 determines if the player has op privileges (SP or opped)
			if (cfg.func_152596_g (getCommandSenderAsPlayer (sender).getGameProfile ()))
				//152612 returns an EPMP from his/her name.
				target = cfg.func_152612_a (args[0]);
			else
			{
				sender.addChatMessage (badperm);
				return;
			}
				
		}
		//As in "/pvp Please enable PvP my good server."
		else
		{
			sender.addChatMessage (help);
			return;
		}
		
		target.getEntityData ().setLong ("PvPTime", MinecraftServer.getSystemTimeMillis () + cooldown);
		
		if (target.getEntityData ().getBoolean ("PvPDenied"))
			cfg.sendChatMsg (new ChatComponentText ("Enabling PvP for " + target.getDisplayName ()));
		else
			sender.addChatMessage (new ChatComponentText ("Disabling PvP for " + target.getDisplayName ()));
	}
	
	@Override 
    public boolean canCommandSenderUseCommand (ICommandSender sender) 
    { 
        return true;
    }
	
	@Override
	public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }
}
