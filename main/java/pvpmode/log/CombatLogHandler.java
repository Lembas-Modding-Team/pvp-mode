package pvpmode.log;

import java.io.File;
import java.util.Date;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public interface CombatLogHandler
{
    public void init(File loggingDir);

    public void log(Date date, EntityPlayer attacker, EntityPlayer victim, float damageAmount,
                    DamageSource damageSource);

    public void cleanup();

}
