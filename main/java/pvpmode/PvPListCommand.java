package pvpmode;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PvPListCommand extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "pvplist";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/pvplist";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		//Safe players are listed in green, PvP-enabled players in red.
		ServerConfigurationManager cfg = MinecraftServer.getServer ().getConfigurationManager ();
		StringBuilder display = new StringBuilder ();
		
		for (Object o : cfg.playerEntityList)
		{
			EntityPlayerMP player = (EntityPlayerMP)o;
			
			if (player.getEntityData ().getBoolean ("PvPDenied"))
				display.append (EnumChatFormatting.GREEN);
			else
				display.append (EnumChatFormatting.RED);
			
			display.append (player.getDisplayName () + " ");
		}
		
		ChatComponentText message = new ChatComponentText (display.toString ());
		sender.addChatMessage (message);
	}
	
	@Override 
    public boolean canCommandSenderUseCommand (ICommandSender sender) 
    { 
        return true;
    }
}
