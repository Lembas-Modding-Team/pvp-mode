package pvpmode.modules.lotr.internal.server;

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
public class LOTRModClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "lotr.common.LOTREventHandler":
                return patchLOTREventHandler (basicClass);
        }
        return basicClass;
    }

    private byte[] patchLOTREventHandler (byte[] basicClass)
    {
        return patchMethod (basicClass, (node) ->
        {
            if (node.name.equals ("onItemUseFinish"))
            {
                InsnList list = new InsnList ();

                list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                list.add (new InsnNode (Opcodes.DUP));
                list.add (
                    new FieldInsnNode (Opcodes.GETFIELD, "net/minecraftforge/event/entity/player/PlayerEvent",
                        "entity", "Lnet/minecraft/entity/Entity;"));
                list.add (
                    new FieldInsnNode (Opcodes.GETFIELD, "net/minecraft/entity/Entity",
                        isObfuscatedEnvironment () ? "field_70170_p" : "worldObj",
                        "Lnet/minecraft/world/World;"));
                list.add (
                    new FieldInsnNode (Opcodes.GETFIELD, "net/minecraft/world/World",
                        isObfuscatedEnvironment () ? "field_72995_K" : "isRemote",
                        "Z"));
                LabelNode label1 = new LabelNode ();
                list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/modules/lotr/api/common/LOTRCommonUtils",
                    "isPoisonBlocked", "(Lnet/minecraftforge/event/entity/player/PlayerUseItemEvent$Finish;)Z", false));
                list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                list.add (new InsnNode (Opcodes.RETURN));
                list.add (label1);

                node.instructions.insertBefore (node.instructions.getFirst (), list);
                logger.info ("Patched \"onItemUseFinish\" in \"lotr.common.LOTREventHandler\"");
                return false;
            }
            return true;
        });
    }
}
