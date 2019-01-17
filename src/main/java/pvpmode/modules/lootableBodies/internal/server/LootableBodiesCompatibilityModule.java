package pvpmode.modules.lootableBodies.internal.server;

import java.nio.file.Path;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cyano.lootable.LootableBodies;
import cyano.lootable.entities.EntityLootableBody;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.*;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent.Drop.Action;

public class LootableBodiesCompatibilityModule extends AbstractCompatibilityModule
{
    
    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder, SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);
        
        MinecraftForge.EVENT_BUS.register (this);
    }

    @SubscribeEvent
    public void onPartialItemDrop (PartialItemDropEvent.Drop event)
    {
        event.setAction (Action.DELETE);
    }

    @SubscribeEvent
    public void onPartialItemPostDrop (PartialItemDropEvent.Post event)
    {

        EntityPlayer player = event.getPlayer ();

        World world = event.getPlayer ().worldObj;

        float rotation = player.getRotationYawHead ();
        EntityLootableBody corpse = new EntityLootableBody (world);
        corpse.setPositionAndRotation (player.posX, player.posY, player.posZ,
            rotation, 0.0f);
        corpse.setDeathTime (world.getTotalWorldTime ());

        event.getRemovedItems ().forEach ( (inventory, stacks) ->
        {
            for (ItemStack stack : stacks)
            {
                switch (inventory)
                {
                    case ARMOUR:
                        if (stack.getItem () instanceof ItemArmor)
                        {
                            ItemArmor item = (ItemArmor) stack.getItem ();

                            // The first armour slot (index: 1) is the boots slot
                            if (corpse.getEquipmentInSlot (3 - item.armorType + 1) == null)
                            {
                                // The slot is free
                                corpse.setCurrentItemOrArmor (3 - item.armorType + 1,
                                    EntityLootableBody.applyItemDamage (stack));
                            }
                            else
                            {
                                // Append to the general inventory because the slot is blocked
                                corpse.vacuumItem (stack);
                            }
                        }
                        break;
                    case HELD:
                        if (corpse.getEquipmentInSlot (0) == null)
                        {
                            // The slot is free
                            corpse.setCurrentItemOrArmor (0, EntityLootableBody.applyItemDamage (stack));
                        }
                        else
                        {
                            // Append to the general inventors because the slot is blocked
                            corpse.vacuumItem (stack);
                        }
                        break;
                    default:
                        corpse.vacuumItem (stack);
                        break;
                }
            }
        });

        if (LootableBodies.addBonesToCorpse)
        {
            corpse.vacuumItem (new ItemStack (Items.rotten_flesh, world.rand.nextInt (3) +
                1));
            corpse.vacuumItem (new ItemStack (Items.bone, world.rand.nextInt (3) + 1));
        }

        world.spawnEntityInWorld (corpse);
        corpse.setOwner (player.getGameProfile ());
        corpse.setRotation (rotation);
    }

}
