package pvpmode.modules.lotr.internal.common.core;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import pvpmode.api.common.core.AbstractClassTransformer;
import pvpmode.api.common.utils.Register;

/**
 * The common class transformer for patches regarding the LOTR Mod.
 * 
 * @author CraftedMods
 *
 */
@Register
public class LOTRModCommonClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "lotr.common.item.LOTRWeaponStats":
                return patchLOTRWeaponStats (basicClass);
            case "lotr.common.item.LOTRItemTermite":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemTermite");
            case "lotr.common.item.LOTRItemConker":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemConker");
            case "lotr.common.item.LOTRItemPebble":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemPebble");
            case "lotr.common.item.LOTRItemSling":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemSling");
            case "lotr.common.item.LOTRItemThrowingAxe":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemThrowingAxe");
            case "lotr.common.item.LOTRItemFirePot":
                return patchOnItemRightClick (basicClass, "lotr.common.item.LOTRItemFirePot");
            case "lotr.common.block.LOTRBlockMug":
                return patchLOTRBlockMug (basicClass);

        }
        return basicClass;
    }

    private byte[] patchLOTRWeaponStats (byte[] basicClass)
    {
        return patchClass (basicClass, "lotr.common.item.LOTRWeaponStats", methodNode ->
        {
            if (methodNode.name.equals ("getTotalArmorValue"))
            {
                patchMethod ("getTotalArmorValue", methodNode, (methNode) ->
                {
                    AbstractInsnNode insertBeforeNode = null;
                    LabelNode jumpLabel = null;

                    for (int i = 0; i < methodNode.instructions.size (); i++)
                    {
                        AbstractInsnNode node = methodNode.instructions.get (i);

                        if (node instanceof VarInsnNode && insertBeforeNode == null)
                        {
                            VarInsnNode varNode1 = (VarInsnNode) node;
                            if (varNode1.getOpcode () == Opcodes.ALOAD && varNode1.var == 3)// ALOAD 3
                            {
                                AbstractInsnNode next = node.getNext ();
                                if (next != null && next.getOpcode () == Opcodes.IFNULL)// IFNULL
                                {
                                    insertBeforeNode = node;
                                }
                            }
                        }
                        else if (insertBeforeNode != null && node.getOpcode () == Opcodes.IADD)
                        {// IADD
                            if (node.getNext () != null && node.getNext () instanceof VarInsnNode)
                            {
                                VarInsnNode nextNode = (VarInsnNode) node.getNext ();
                                if (nextNode.getOpcode () == Opcodes.ISTORE && nextNode.var == 1)
                                {// ISTORE 1
                                    if (nextNode.getNext () != null && nextNode.getNext () instanceof LabelNode)
                                    {
                                        jumpLabel = (LabelNode) nextNode.getNext ();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (insertBeforeNode != null && jumpLabel != null)
                        return Pair.of (insertBeforeNode, jumpLabel);
                    return null;
                }, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();
                    list.add (new VarInsnNode (Opcodes.ALOAD, 0));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 3));
                    list.add (new FieldInsnNode (Opcodes.GETSTATIC,
                        "pvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType", "PROTECT",
                        "Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/api/common/utils/PvPCommonUtils",
                        "isValidArmorItemForEntity",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;)Z",
                        false));
                    list.add (new JumpInsnNode (Opcodes.IFEQ, preCondition.getRight ()));
                    methodNode.instructions.insertBefore (preCondition.getLeft (), list);
                    return true;
                });
                return false;
            }
            return true;
        });
    }

    private byte[] patchOnItemRightClick (byte[] basicClass, String className)
    {
        return patchClass (basicClass, className, methodNode ->
        {
            if (methodNode.name.equals ("onItemRightClick") || methodNode.name.equals ("func_77659_a"))
            {
                patchMethod ("onItemRightClick", methodNode, (methNode) -> true, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();

                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 3));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC,
                        "pvpmode/modules/lotr/internal/common/core/LOTRModCommonClassTransformer",
                        "onItemRightClickPatch",
                        "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                    list.add (new InsnNode (Opcodes.F_SAME));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new InsnNode (Opcodes.ARETURN));
                    list.add (label1);

                    methodNode.instructions.insertBefore (methodNode.instructions.getFirst (), list);
                    return true;
                });
                return false;
            }
            return true;
        });
    }

    public static boolean onItemRightClickPatch (ItemStack stack, EntityPlayer player)
    {
        return MinecraftForge.EVENT_BUS.post (new PlayerUseItemEvent.Start (player, stack, 1));
    }

    private byte[] patchLOTRBlockMug (byte[] basicClass)
    {
        return patchClass (basicClass, "lotr.common.block.LOTRBlockMug", methodNode ->
        {
            if (methodNode.name.equals ("onBlockActivated") || methodNode.name
                .equals ("func_149727_a"))
            {

                patchMethod ("onBlockActivated", methodNode, (methNode) ->
                {
                    AbstractInsnNode insertAfterNode = null;
                    LabelNode jumpLabel = null;

                    for (int i = 0; i < methodNode.instructions.size (); i++)
                    {
                        AbstractInsnNode node = methodNode.instructions.get (i);

                        if (node instanceof VarInsnNode)
                        {
                            VarInsnNode varNode = (VarInsnNode) node;
                            if (varNode.getOpcode () == Opcodes.ALOAD && varNode.var == 5) // ALOAD 5
                            {
                                if (varNode.getNext () != null)
                                {
                                    node = varNode.getNext ();
                                    if (node instanceof MethodInsnNode)
                                    {
                                        MethodInsnNode functionNode = (MethodInsnNode) node;
                                        if (functionNode.getOpcode () == Opcodes.INVOKEVIRTUAL
                                            && functionNode.owner.equals ("lotr/common/item/LOTRItemMug")
                                            && functionNode.desc
                                                .equals ("(Lnet/minecraft/entity/player/EntityPlayer;)Z")
                                            && functionNode.name.equals ("canPlayerDrink")) // INVOKEVIRTUAL
                                                                                            // lotr/common/item/LOTRItemMug.canPlayerDrink(Lnet/minecraft/entity/player/EntityPlayer;)Z
                                        {
                                            if (functionNode.getNext () != null)
                                            {
                                                node = functionNode.getNext ();
                                                if (node instanceof VarInsnNode)
                                                {
                                                    varNode = (VarInsnNode) node;
                                                    if (varNode.getOpcode () == Opcodes.ISTORE && varNode.var == 16)
                                                    { // ISTORE 16
                                                        insertAfterNode = varNode;
                                                        if (varNode.getNext () != null)
                                                        {
                                                            node = varNode.getNext ();
                                                            if (node instanceof LabelNode)
                                                            {
                                                                jumpLabel = (LabelNode) node;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        {

                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (insertAfterNode != null && jumpLabel != null)
                        return Pair.of (insertAfterNode, jumpLabel);
                    return null;

                }, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();

                    list.add (new VarInsnNode (Opcodes.ILOAD, 16));
                    list.add (new JumpInsnNode (Opcodes.IFEQ, preCondition.getRight ()));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 14));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 5));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC,
                        "pvpmode/modules/lotr/internal/common/core/LOTRModCommonClassTransformer",
                        "onItemRightClickPatch",
                        "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                        false));
                    list.add (new JumpInsnNode (Opcodes.IFEQ, preCondition.getRight ()));
                    list.add (new InsnNode (Opcodes.ICONST_0));
                    list.add (new VarInsnNode (Opcodes.ISTORE, 16));

                    methNode.instructions.insert (preCondition.getLeft (), list);
                    return true;
                });
                return false;
            }
            return true;
        });
    }

}
