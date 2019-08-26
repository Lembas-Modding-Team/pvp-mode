package pvpmode.modules.lootableBodies.internal.server;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import pvpmode.api.common.core.*;
import pvpmode.api.common.utils.Register;

/**
 * The server-side class transformer for the lootable bodies compatibility
 * module.
 * 
 * @author CraftedMods
 *
 */
@Register(properties = CoremodEnvironmentConstants.SIDE_KEY + "=" + CoremodEnvironmentConstants.SIDE_SERVER)
public class LootableBodiesClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "cyano.lootable.events.PlayerDeathEventHandler":
                return patchLootableBodiesEventHandler (basicClass);
        }
        return basicClass;
    }

    private byte[] patchLootableBodiesEventHandler (byte[] basicClass)
    {
        return patchClass (basicClass, "cyano.lootable.events.PlayerDeathEventHandler", (node) ->
        {
            if (node.name.equals ("playerDeathEvent"))
            {
                patchMethod ("playerDeathEvent", node, (methNode) -> true, (methNode, preCondition) ->
                {
                    InsnList list = new InsnList ();

                    list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                    list.add (
                        new FieldInsnNode (Opcodes.GETFIELD, "net/minecraftforge/event/entity/living/LivingDeathEvent",
                            "entity", "Lnet/minecraft/entity/Entity;"));
                    list.add (
                        new FieldInsnNode (Opcodes.GETFIELD, "net/minecraft/entity/Entity",
                            isObfuscatedEnvironment () ? "field_70170_p" : "worldObj",
                            "Lnet/minecraft/world/World;"));
                    list.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "net/minecraft/world/World",
                        isObfuscatedEnvironment () ? "func_82736_K" : "getGameRules",
                        "()Lnet/minecraft/world/GameRules;",
                        false));
                    list.add (new LdcInsnNode ("keepInventory"));
                    list.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "net/minecraft/world/GameRules",
                        isObfuscatedEnvironment () ? "func_82766_b" : "getGameRuleBooleanValue",
                        "(Ljava/lang/String;)Z",
                        false));
                    LabelNode label1 = new LabelNode ();
                    list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                    list.add (new InsnNode (Opcodes.F_SAME));
                    list.add (new InsnNode (Opcodes.RETURN));
                    list.add (label1);

                    node.instructions.insertBefore (node.instructions.getFirst (), list);
                    return true;
                });
                return false;
            }
            return true;
        });
    }

}
