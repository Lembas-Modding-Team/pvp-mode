package pvpmode.modules.lotr.internal.server.core;

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import pvpmode.api.common.core.*;
import pvpmode.api.common.utils.Register;

/**
 * The server class transformer for patches regarding the LOTR Mod.
 * 
 * @author CraftedMods
 *
 */
@Register(properties = CoremodEnvironmentConstants.SIDE_KEY + "=" + CoremodEnvironmentConstants.SIDE_SERVER)
public class LOTRModServerClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "lotr.common.LOTRLevelData":
                return patchLOTRLevelData (basicClass);
        }
        return basicClass;
    }

    public byte[] patchLOTRLevelData (byte[] basicClass)
    {
        return patchClass (basicClass, "LOTRLevelData", methodNode ->
        {
            return !patchMethod ("sendPlayerLocationsToPlayer", methodNode, methNode ->
            {

                AbstractInsnNode insertBeforeNode = null;
                LabelNode jumpLabel = null;

                for (int i = 0; i < methodNode.instructions.size (); i++)
                {
                    AbstractInsnNode node = methodNode.instructions.get (i);

                    if (node instanceof VarInsnNode)
                    {
                        VarInsnNode varNode1 = (VarInsnNode) node;
                        if (varNode1.getOpcode () == Opcodes.ILOAD && varNode1.var == 3)// ILOAD 3
                        {
                            if (varNode1.getNext () != null && varNode1.getNext () instanceof JumpInsnNode)
                            {
                                JumpInsnNode next = (JumpInsnNode) varNode1.getNext ();
                                if (next.getOpcode () == Opcodes.IFEQ && next.getNext () != null
                                    && next.getNext () instanceof VarInsnNode) // IFEQ
                                {
                                    VarInsnNode varNode2 = (VarInsnNode) next.getNext ();
                                    if (varNode2.getOpcode () == Opcodes.ILOAD && varNode2.var == 4 // ILOAD 4
                                        && varNode2.getNext () != null && varNode2.getNext () instanceof JumpInsnNode)
                                    {
                                        JumpInsnNode next2 = (JumpInsnNode) varNode2.getNext ();
                                        if (next2.getOpcode () == Opcodes.IFEQ)
                                        { // IFEQ
                                            insertBeforeNode = varNode1;
                                            jumpLabel = next2.label;
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                if (insertBeforeNode != null && jumpLabel != null)
                    return Pair.of (insertBeforeNode, jumpLabel);
                return null;
            }, (node, preCondition) ->
            {
                InsnList list = new InsnList ();
                list.add (new VarInsnNode (Opcodes.ALOAD, 0));
                list.add (new VarInsnNode (Opcodes.ALOAD, 8));
                list.add (new MethodInsnNode (Opcodes.INVOKESTATIC,
                    "pvpmode/modules/lotr/internal/server/core/LOTRModServerSideHooks",
                    "getMapLocationVisibility",
                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                    false));
                list.add (new VarInsnNode (Opcodes.ISTORE, 9));
                node.instructions.insertBefore (preCondition.getLeft (), list);
                return true;
            });
        });
    }

}
