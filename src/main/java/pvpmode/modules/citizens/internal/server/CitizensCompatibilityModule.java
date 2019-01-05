package pvpmode.modules.citizens.internal.server;

import java.lang.reflect.*;
import java.nio.file.Path;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.events.PlayerIdentityCheckEvent;

public class CitizensCompatibilityModule extends AbstractCompatibilityModule
{

    private Method getBukkitEntityMethod;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        getBukkitEntityMethod = EntityPlayerMP.class.getMethod ("getBukkitEntity");
        getBukkitEntityMethod.setAccessible (true); // Just for the case it isn't
        
        MinecraftForge.EVENT_BUS.register (this);
    }

    @SubscribeEvent
    public void onPlayerIdentityCheck (PlayerIdentityCheckEvent event)
    {
        try
        {
            CraftEntity bukkitEntity = (CraftEntity) getBukkitEntityMethod.invoke (event.getPlayer ());
            if (bukkitEntity.hasMetadata ("NPC"))
            {
                event.setCanceled (true);
            }
        }
        catch (IllegalAccessException e)
        {
            logger.errorThrowable ("Couldn't access the function \"getBukkitEntity\"", e);
        }
        catch (IllegalArgumentException e)
        {
            logger.errorThrowable ("The function \"getBukkitEntity\" was invoked will illegal arguments", e);
        }
        catch (InvocationTargetException e)
        {
            logger.errorThrowable ("The function \"getBukkitEntity\" threw an exception upon invokation", e);
        }
    }

}
