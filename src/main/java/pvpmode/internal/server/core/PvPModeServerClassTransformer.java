package pvpmode.internal.server.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import pvpmode.api.common.core.*;
import pvpmode.api.common.utils.Register;

/**
 * The server-side class transformer.
 * 
 * @author CraftedMods
 *
 */
@Register(properties = CoremodEnvironmentConstants.SIDE_KEY + "=" + CoremodEnvironmentConstants.SIDE_SERVER)
public class PvPModeServerClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "net.minecraft.inventory.ContainerPlayer":
            case "aap":
                return patchContainerPlayer (basicClass);
        }
        return basicClass;
    }

    private byte[] patchContainerPlayer (byte[] basicClass)
    {
        return patchClass (basicClass, "net.minecraft.inventory.ContainerPlayer", (node) ->
        {
            return !patchMethod ("transferStackInSlot", "func_82846_b", node, (methNode) -> true,
                (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();

                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/api/server/utils/PvPServerUtils",
                        "isShiftClickingBlocked",
                        String.format ("(L%s;)Z", "net/minecraft/entity/player/EntityPlayer"),
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                    list.add (new InsnNode (Opcodes.F_SAME));
                    list.add (new InsnNode (Opcodes.ACONST_NULL));
                    list.add (new InsnNode (Opcodes.ARETURN));
                    list.add (label1);

                    node.instructions.insertBefore (node.instructions.getFirst (), list);
                    return true;
                });
        });
    }
}
