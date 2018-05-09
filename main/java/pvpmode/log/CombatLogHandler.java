package pvpmode.log;

import java.util.Date;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public interface CombatLogHandler
{
    public void init(FMLPreInitializationEvent event);

    public void log(Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
                    DamageSource damageSource);

    public void cleanup();

}
