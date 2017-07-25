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
		+ "/pvp <on:off> [player]");
	
	@Override
	public int getRequiredPermissionLevel ()
	{
		return 0;
	}
	
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
		
		//As in "/pvp on"
		if (args.length == 1)
			target = getCommandSenderAsPlayer (sender);
		//As in "/pvp on VulcanForge"
		else if (args.length == 2)
		{
			//152596 determines if the player has op privileges (SP or opped)
			if (cfg.func_152596_g (getCommandSenderAsPlayer (sender).getGameProfile ()))
				//152612 returns an EPMP from his/her name.
				target = cfg.func_152612_a (args[1]);
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
		
		if (args[0].equals ("on"))
		{
			target.getEntityData ().setBoolean ("PvPDenied", false);
			cfg.sendChatMsg (new ChatComponentText ("PvP is now enabled for " + target.getDisplayName ()));
		}
		else if (args[0].equals ("off"))
		{
			target.getEntityData ().setBoolean ("PvPDenied", true);
			cfg.sendChatMsg (new ChatComponentText ("PvP is now disabled for " + target.getDisplayName ()));
		}
		else
			sender.addChatMessage (help);
	}
}
