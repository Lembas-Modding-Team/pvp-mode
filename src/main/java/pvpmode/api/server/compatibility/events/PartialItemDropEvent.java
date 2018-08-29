package pvpmode.api.server.compatibility.events;

import java.util.*;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pvpmode.api.common.utils.PvPCommonUtils;

/**
 * An abstract superclass for events fired for items which will be dropped with
 * the partial inventory loss.
 *
 * @author CraftedMods
 *
 */
public abstract class PartialItemDropEvent extends Event
{

    /**
     * The part of the player's inventory an item was in.
     *
     * @author CraftedMods
     *
     */
    public enum EnumInventory// TODO: Externalize to utils
    {
    ARMOUR, HOTBAR, MAIN, HELD;
    }

    private final EntityPlayer player;

    public PartialItemDropEvent (EntityPlayer player)
    {
        this.player = player;
    }

    /**
     * The player who died.
     */
    public EntityPlayer getPlayer ()
    {
        return player;
    }

    /**
     * This event will be fired when the partial inventory loss algorithm picked the
     * items that should be dropped.
     *
     * @author CraftedMods
     *
     */
    public static class Pre extends PartialItemDropEvent
    {

        private final Map<EnumInventory, List<ItemStack>> itemsToBeDropped;
        private final boolean isPvPDeath;

        public Pre (EntityPlayer player, Map<EnumInventory, List<ItemStack>> itemsToBeDropped, boolean isPvPDeath)
        {
            super (player);
            this.itemsToBeDropped = PvPCommonUtils.deepUnmodifiableMap (itemsToBeDropped);
            this.isPvPDeath = isPvPDeath;
        }

        /**
         * Contains a list of all items that the partial inventory loss algorithm picked
         * to be dropped. The map contains the items sorted by the inventory they are
         * in, and in the order they are in the inventory (excluding items that won't be
         * processed by the algorithm).
         */
        public Map<EnumInventory, List<ItemStack>> getItemsToBeDropped ()
        {
            return itemsToBeDropped;
        }

        /**
         * Returns whether the death was caused by PvP or not (then by PvE).
         */
        public boolean isPvPDeath ()
        {
            return isPvPDeath;
        }

    }

    /**
     * This event will be fired for every item that should be dropped. One can
     * specify via the action whether the item really should be dropped and/or
     * removed from the inventory, or if nothing should happen. The event for each
     * item will be invoked in the order the items are stored in the referenced
     * inventory. The items will be removed or dropped after all events for each and
     * all items have been fired.
     *
     * @author CraftedMods
     *
     */
    public static class Drop extends PartialItemDropEvent
    {

        /**
         * DROP means that the stack will be dropped, but not removed from the
         * inventory.<br/>
         * DELETE_AND_DROP means that the stack will be removed from the inventory and
         * dropped. <br/>
         * DELETE means that the stack just will be removed.<br/>
         * NOTHING means that the stack will remain in the player's inventory.
         *
         * @author CraftedMods
         *
         */
        public enum Action
        {
        DROP, DELETE_AND_DROP, DELETE, NOTHING
        }

        private final ItemStack stack;

        private Action action = Action.DELETE_AND_DROP;

        private final EnumInventory inventory;

        public Drop (EntityPlayer player, ItemStack stack, EnumInventory inventory)
        {
            super (player);
            this.stack = stack;
            this.inventory = inventory;
        }

        /**
         * Returns the stack that should be dropped
         */
        public ItemStack getStack ()
        {
            return stack;
        }

        /**
         * Returns the action the algorithm will do with the specified stack.
         */
        public Action getAction ()
        {
            return action;
        }

        /**
         * Sets the action that will be done after all events were supplied to their
         * listeners. This overrides the last setted action.
         */
        public void setAction (Action action)
        {
            Objects.requireNonNull (action);
            this.action = action;
        }

        /**
         * Returns the inventory of the player where item stack was.
         */
        public EnumInventory getInventory ()
        {
            return inventory;
        }

    }

    /**
     * An event that will be fired after the items computed by the partial inventory
     * loss algorithm were dropped or removed.
     *
     * @author CraftedMods
     *
     */
    public static class Post extends PartialItemDropEvent
    {

        private final Map<EnumInventory, List<ItemStack>> droppedItems;

        private final Map<EnumInventory, List<ItemStack>> removedItems;

        private final Map<EnumInventory, List<ItemStack>> unprocessedItems;

        public Post (EntityPlayer player, Map<EnumInventory, List<ItemStack>> droppedItems,
            Map<EnumInventory, List<ItemStack>> removedItems, Map<EnumInventory, List<ItemStack>> unprocessedItems)
        {
            super (player);
            this.droppedItems = PvPCommonUtils.deepUnmodifiableMap (droppedItems);
            this.removedItems = PvPCommonUtils.deepUnmodifiableMap (removedItems);
            this.unprocessedItems = PvPCommonUtils.deepUnmodifiableMap (unprocessedItems);
        }

        /**
         * Returns all items that were dropped, sorted by the inventory they were and by
         * their position in that inventory.
         */
        public Map<EnumInventory, List<ItemStack>> getDroppedItems ()
        {
            return droppedItems;
        }

        /**
         * Returns all items that were removed from the player's inventory, sorted by
         * the inventory they were and by their position in that inventory.
         */
        public Map<EnumInventory, List<ItemStack>> getRemovedItems ()
        {
            return removedItems;
        }

        /**
         * Returns all items that were determined to be dropped by the partial inventory
         * loss algorithm but weren't dropped nor removed from the inventory, sorted by
         * the inventory they were and by their position in that inventory.
         */
        public Map<EnumInventory, List<ItemStack>> getUnprocessedItems ()
        {
            return unprocessedItems;
        }

    }

}
