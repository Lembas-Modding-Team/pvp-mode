package pvpmode.api.server.compatibility.events;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.item.ItemStack;

/**
 * An event fired for every item in the player's inventory upon death if the
 * partial inventory loss is enabled. Canceling the event prevents the stack
 * from being added to a list of items that *could* be dropped.
 *
 * @author CraftedMods
 *
 */
@Cancelable
public class PartialItemLossEvent extends Event
{

    private final ItemStack stack;

    public PartialItemLossEvent (ItemStack stack)
    {
        this.stack = stack;
    }

    /**
     * Returns the item stack to be dropped
     */
    public ItemStack getStack ()
    {
        return stack;
    }

}
