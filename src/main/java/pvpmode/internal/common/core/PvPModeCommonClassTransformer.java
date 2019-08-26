package pvpmode.internal.common.core;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import pvpmode.api.common.compatibility.events.ArmorItemCheckEvent.CheckType;
import pvpmode.api.common.core.AbstractClassTransformer;
import pvpmode.api.common.utils.Register;
import pvpmode.api.server.utils.PvPServerUtils;

/**
 * The class transformer of the PvP Mode Mod, which is executed server- and
 * clientside.
 * 
 * @author CraftedMods
 *
 */
@Register
public class PvPModeCommonClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "net.minecraftforge.common.ISpecialArmor$ArmorProperties":
                return patchISpecialArmor (basicClass);
            case "net.minecraft.inventory.ContainerPlayer$1":
            case "aaq":
                return patchContainerPlayerArmorSlot (basicClass);
            case "net.minecraft.inventory.ContainerPlayer":
            case "aap":
                return patchContainerPlayer (basicClass);
            case "abb":
            case "net.minecraft.item.ItemArmor":
                return patchItemArmor (basicClass);
        }
        return basicClass;
    }

    private byte[] patchContainerPlayer (byte[] basicClass)
    {
        return patchClass (basicClass, "net.minecraft.inventory.ContainerPlayer", (methodNode) ->
        {
            if (methodNode.name.equals ("transferStackInSlot") || methodNode.name
                .equals ("func_82846_b"))
            {
                this.patchMethod ("transferStackInSlot", methodNode, (methNode) ->
                {
                    AbstractInsnNode insertAfterNode = null;

                    for (int i = 0; i < methodNode.instructions.size (); i++)
                    {
                        AbstractInsnNode node = methodNode.instructions.get (i);

                        if (node.getOpcode () == Opcodes.IADD)
                        {// IADD
                            AbstractInsnNode next = node.getNext ();
                            if (next != null && next instanceof VarInsnNode)
                            {
                                VarInsnNode varNode = (VarInsnNode) next;
                                if (varNode.var == 6 && varNode.getOpcode () == Opcodes.ISTORE)
                                {
                                    insertAfterNode = varNode;
                                    break;
                                }
                            }
                        }
                    }
                    return insertAfterNode;
                }, (methNode, insertAfterNode) ->
                {
                    InsnList list = new InsnList ();
                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 5));
                    list.add (new VarInsnNode (Opcodes.ILOAD, 2));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC,
                        "pvpmode/internal/common/core/PvPModeCommonClassTransformer",
                        "patchContainerPlayerCondition",
                        "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;I)Z",
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                    list.add (new InsnNode (Opcodes.F_SAME));
                    list.add (new InsnNode (Opcodes.ACONST_NULL));
                    list.add (new InsnNode (Opcodes.ARETURN));
                    list.add (label1);

                    methodNode.instructions.insert (insertAfterNode, list);
                    return true;
                });
                return false;

            }
            return true;
        });
    }

    public static boolean patchContainerPlayerCondition (EntityPlayer player, ItemStack stack, int slot)
    {
        return !PvPServerUtils.isValidArmorItemForEntity (player, stack, CheckType.EQUIP);
    }

    private byte[] patchISpecialArmor (byte[] basicClass)
    {
        return patchClass (basicClass, "net.minecraftforge.common.ISpecialArmor$ArmorProperties", methodNode ->
        {
            if (methodNode.name.equals ("ApplyArmor"))
            {
                patchMethod ("ApplyArmor", methodNode, (methNode) ->
                {
                    int insertIndex = -1;
                    Label label = null;
                    for (int i = 0; i < methodNode.instructions.size (); i++)
                    {
                        AbstractInsnNode node = methodNode.instructions.get (i);

                        if (node instanceof VarInsnNode)
                        {
                            VarInsnNode varNode = (VarInsnNode) node;
                            if (insertIndex == -1)
                            {
                                if (varNode.getOpcode () == Opcodes.ALOAD && varNode.var == 1)
                                { // ALOAD 1
                                    AbstractInsnNode nextNode = varNode.getNext ();
                                    if (nextNode != null && nextNode instanceof VarInsnNode)
                                    {
                                        varNode = (VarInsnNode) nextNode;
                                        if (varNode.getOpcode () == Opcodes.ILOAD && varNode.var == 6)
                                        { // ILOAD 6
                                            nextNode = varNode.getNext ();
                                            if (nextNode != null && nextNode instanceof InsnNode)
                                            {
                                                if (nextNode.getOpcode () == Opcodes.AALOAD)
                                                { // AALOAD
                                                    nextNode = nextNode.getNext ();
                                                    if (nextNode != null && nextNode instanceof VarInsnNode)
                                                    {
                                                        varNode = (VarInsnNode) nextNode;
                                                        if (varNode.getOpcode () == Opcodes.ASTORE && varNode.var == 7)
                                                        { // ASTORE 7
                                                            insertIndex = i + 3;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            {
                                if (varNode.getOpcode () == Opcodes.ALOAD && varNode.var == 7)
                                {
                                    AbstractInsnNode nextNode = varNode.getNext ();
                                    if (nextNode != null && nextNode instanceof JumpInsnNode)
                                    {
                                        JumpInsnNode jumpNode = (JumpInsnNode) nextNode;
                                        if (jumpNode.getOpcode () == Opcodes.IFNONNULL)
                                        {
                                            nextNode = jumpNode.getNext ();
                                            if (nextNode != null && nextNode instanceof LabelNode)
                                            {
                                                label = ((LabelNode) nextNode).getLabel ();
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (insertIndex != -1 && label != null)
                        return Pair.of (insertIndex, label);
                    return null;
                }, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();

                    list.add (new VarInsnNode (Opcodes.ALOAD, 0));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 7));
                    list.add (new FieldInsnNode (Opcodes.GETSTATIC,
                        "pvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType", "PROTECT",
                        "Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/api/common/utils/PvPCommonUtils",
                        "isValidArmorItemForEntity",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;)Z",
                        false));
                    list.add (new JumpInsnNode (Opcodes.IFEQ, new LabelNode (preCondition.getRight ())));
                    methodNode.instructions.insert (methodNode.instructions.get (preCondition.getLeft ()), list);
                    return true;
                });

                return false;
            }
            return true;
        });
    }

    private byte[] patchContainerPlayerArmorSlot (byte[] basicClass)
    {
        return patchClass (basicClass, "net.minecraft.inventory.ContainerPlayer$1", (node) ->
        {
            if (node.name.equals ("isItemValid") || node.name.equals ("func_75214_a"))
            {
                patchMethod ("isItemValid", node, (methNode) -> true, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();
                    list.add (new VarInsnNode (Opcodes.ALOAD, 0));
                    list.add (
                        new FieldInsnNode (Opcodes.GETFIELD, "net/minecraft/inventory/ContainerPlayer$1", "this$0",
                            "Lnet/minecraft/inventory/ContainerPlayer;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "net/minecraft/inventory/ContainerPlayer",
                        "access$000",
                        "(Lnet/minecraft/inventory/ContainerPlayer;)Lnet/minecraft/entity/player/EntityPlayer;",
                        false));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new FieldInsnNode (Opcodes.GETSTATIC,
                        "pvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType",
                        "EQUIP",
                        "Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC,
                        "pvpmode/api/common/utils/PvPCommonUtils",
                        "isValidArmorItemForEntity",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;)Z",
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFNE, label1));
                    list.add (new InsnNode (Opcodes.ICONST_0));
                    list.add (new InsnNode (Opcodes.IRETURN));
                    list.add (label1);

                    node.instructions.insertBefore (node.instructions.getFirst (), list);
                    return true;
                });
                return false;
            }
            return true;
        });
    }

    private byte[] patchItemArmor (byte[] basicClass)
    {
        return patchClass (basicClass, "net.minecraft.item.ItemArmor", methodNode ->
        {
            if (methodNode.name.equals ("onItemRightClick") || methodNode.name.equals ("func_77659_a"))
            {
                patchMethod ("onItemRightClick", methodNode, (methNode) -> true, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();
                    list.add (new VarInsnNode (Opcodes.ALOAD, 3));
                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new FieldInsnNode (Opcodes.GETSTATIC,
                        "pvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType", "EQUIP",
                        "Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/api/common/utils/PvPCommonUtils",
                        "isValidArmorItemForEntity",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;Lpvpmode/api/common/compatibility/events/ArmorItemCheckEvent$CheckType;)Z",
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFNE, label1));
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

}
