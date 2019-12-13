package pvpmode.modules.lotr.internal.common.network;

import java.util.*;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.modules.lotr.api.common.gear.EnumGearBlockingCondition;
import pvpmode.modules.lotr.internal.client.LOTRModClientCompatibilityModule;
import pvpmode.modules.lotr.internal.common.*;

public class BlockedGearItemsListChangedMessage implements IMessage
{

    private Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> blockedItems;

    public BlockedGearItemsListChangedMessage ()
    {

    }

    public BlockedGearItemsListChangedMessage (Map<Item, Map<FactionEntry, EnumGearBlockingCondition>> blockedItems)
    {
        this.blockedItems = blockedItems;
    }

    @Override
    public void fromBytes (ByteBuf buf)
    {
        blockedItems = new HashMap<> ();

        int size = buf.readInt ();

        for (int i = 0; i < size; i++)
        {
            NBTTagCompound tag = ByteBufUtils.readTag (buf);

            Item item = (Item) Item.itemRegistry.getObject (tag.getString ("Item"));

            blockedItems.put (item, new HashMap<> ());

            NBTTagList factionEntriesList = tag.getTagList ("FactionEntries", 10);

            for (int j = 0; j < factionEntriesList.tagCount (); j++)
            {
                NBTTagCompound factionEntryTag = factionEntriesList.getCompoundTagAt (j);

                blockedItems.get (item).put (FactionEntry.readFromNBT (factionEntryTag),
                    EnumGearBlockingCondition.valueOf (factionEntryTag.getString ("Condition")));
            }
        }
    }

    @Override
    public void toBytes (ByteBuf buf)
    {
        buf.writeInt (blockedItems.size ());
        for (Map.Entry<Item, Map<FactionEntry, EnumGearBlockingCondition>> entry : blockedItems.entrySet ())
        {
            NBTTagCompound entryTag = new NBTTagCompound ();

            entryTag.setString ("Item", Item.itemRegistry.getNameForObject (entry.getKey ()));

            NBTTagList factionEntriesList = new NBTTagList ();
            for (Map.Entry<FactionEntry, EnumGearBlockingCondition> factionDataEntry : entry.getValue ().entrySet ())
            {
                FactionEntry factionEntry = factionDataEntry.getKey ();

                NBTTagCompound factionEntryTag = new NBTTagCompound ();

                factionEntry.writeToNBT (factionEntryTag);
                factionEntryTag.setString ("Condition", factionDataEntry.getValue ().name ());

                factionEntriesList.appendTag (factionEntryTag);
            }

            entryTag.setTag ("FactionEntries", factionEntriesList);

            ByteBufUtils.writeTag (buf, entryTag);

        }

    }

    public static class BlockedGearItemsListChangedMessageHandler
        implements IMessageHandler<BlockedGearItemsListChangedMessage, BlockedGearItemsListChangedMessage>
    {

        @Override
        public BlockedGearItemsListChangedMessage onMessage (BlockedGearItemsListChangedMessage message,
            MessageContext ctx)
        {
            PvPCommonUtils.executeForCompatibilityModule (LOTRModCompatibilityModuleLoader.class,
                LOTRModClientCompatibilityModule.class, (loader, module) ->
                {
                    module.getBlockedGearManager ().setBlockedItems (message.blockedItems);
                });
            return null;
        }

    }

}
