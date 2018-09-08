package pvpmode.core;

import java.util.Iterator;
import java.util.function.Predicate;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import cpw.mods.fml.common.eventhandler.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class PvPModeClassTransformer implements IClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        switch (name)
        {
            case "net.minecraft.inventory.ContainerPlayer":
            case "aap":
                return patchContainerPlayer (basicClass, name.equals ("aap"));
            case "cyano.lootable.events.PlayerDeathEventHandler":
                return patchLootableBodiesEventHandler (basicClass);
        }

        return basicClass;
    }

    private byte[] patchContainerPlayer (byte[] basicClass, boolean obfuscated)
    {
        return this.patchMethod (basicClass, (node) ->
        {
            if (node.name.equals ("transferStackInSlot") || node.name.equals ("func_82846_b"))
            {
                InsnList list = new InsnList ();

                list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/PvPUtils", "isShiftClickingBlocked",
                    String.format ("(L%s;)Z", "net/minecraft/entity/player/EntityPlayer"),
                    false));
                LabelNode label1 = new LabelNode ();
                list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                list.add (new InsnNode (Opcodes.F_SAME));
                list.add (new InsnNode (Opcodes.ACONST_NULL));
                list.add (new InsnNode (Opcodes.ARETURN));
                list.add (label1);

                node.instructions.insertBefore (node.instructions.getFirst (), list);
                System.out.println ("Patched \"transferStackInSlot\" in \"net.minecraft.inventory.ContainerPlayer\"");
                return false;
            }
            return true;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void playerDeathEvent (LivingDeathEvent event)
    {
        if (event.entity.worldObj.getGameRules ().getGameRuleBooleanValue ("keepInventory"))
            return;
    }

    private byte[] patchLootableBodiesEventHandler (byte[] basicClass)
    {
        return this.patchMethod (basicClass, (node) ->
        {
            if (node.name.equals ("playerDeathEvent"))
            {
                InsnList list = new InsnList ();

                list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                list.add (
                    new FieldInsnNode (Opcodes.GETFIELD, "net/minecraftforge/event/entity/living/LivingDeathEvent",
                        "entity", "Lnet/minecraft/entity/Entity;"));
                list.add (
                    new FieldInsnNode (Opcodes.GETFIELD, "net/minecraft/entity/Entity",
                        PvPModeCore.obfuscatedEnvironment ? "field_70170_p" : "worldObj",
                        "Lnet/minecraft/world/World;"));
                list.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "net/minecraft/world/World",
                    PvPModeCore.obfuscatedEnvironment ? "func_82736_K" : "getGameRules",
                    "()Lnet/minecraft/world/GameRules;",
                    false));
                list.add (new LdcInsnNode ("keepInventory"));
                list.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "net/minecraft/world/GameRules",
                    PvPModeCore.obfuscatedEnvironment ? "func_82766_b" : "getGameRuleBooleanValue",
                    "(Ljava/lang/String;)Z",
                    false));
                LabelNode label1 = new LabelNode ();
                list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                list.add (new InsnNode (Opcodes.F_SAME));
                list.add (new InsnNode (Opcodes.RETURN));
                list.add (label1);

                node.instructions.insertBefore (node.instructions.getFirst (), list);
                System.out
                    .println ("Patched \"playerDeathEvent\" in \"cyano.lootable.events.PlayerDeathEventHandler\"");
                return false;
            }
            return true;
        });
    }

    /**
     * Iterates through all methods of the specified class and allows patches for
     * them. If the processor returns false, the iteration will be stopped.
     * 
     * @param basicClass
     *            The unmodified class
     * @param methodProcessor
     *            A processor called for every method
     * @return The patched class
     */
    private byte[] patchMethod (byte[] basicClass, Predicate<MethodNode> methodProcessor)
    {
        ClassNode classNode = new ClassNode ();
        ClassReader classReader = new ClassReader (basicClass);
        classReader.accept (classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator ();
        while (methods.hasNext ())
        {
            if (!methodProcessor.test (methods.next ()))
                break;
        }

        ClassWriter writer = new ClassWriter (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept (writer);
        return writer.toByteArray ();
    }

}
