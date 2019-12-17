package pvpmode.api.common.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import pvpmode.api.common.utils.PvPCommonUtils;

/**
 * An event that will be fired if
 * {@link PvPCommonUtils#isValidArmorItemForEntity(Entity, ItemStack, CheckType)}
 * is called, for determining whether the supplied item is a valid armor item
 * for the supplied entity, with the supplied action. If the event is canceled,
 * it's assumed that the armor item isn't a valid one.
 * 
 * @author CraftedMods
 *
 */
@Cancelable
public class ArmorItemCheckEvent extends Event
{

    /**
     * What to check exactly. {@link ArmorItemCheckEvent.CheckType#EQUIP} means that
     * the event checks whether the armor item can be equipped.
     * {@link ArmorItemCheckEvent.CheckType#PROTECT} means that the event checks
     * whether the armor item can protect the supplied entity.
     * 
     * @author CraftedMods
     *
     */
    public enum CheckType
    {
    EQUIP, PROTECT;
    }

    private final Entity entity;
    private final ItemStack armorItem;
    private final CheckType checkType;

    public ArmorItemCheckEvent (Entity entity, ItemStack armorItem, CheckType checkType)
    {
        this.entity = entity;
        this.armorItem = armorItem;
        this.checkType = checkType;
    }

    /**
     * The entity who wears the armor item.
     * 
     * @return The entity who wears the armor item
     */
    public Entity getEntity ()
    {
        return entity;
    }

    /**
     * The armor item stack.
     * 
     * @return The armor item stack
     */
    public ItemStack getArmorItem ()
    {
        return armorItem;
    }

    /**
     * Gets the action the event checks.
     * 
     * @return The action to check
     */
    public CheckType getCheckType ()
    {
        return checkType;
    }

}
