package pvpmode.core;

import java.util.Iterator;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;

public class PvPModeClassTransformer implements IClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        if (name.equals ("net.minecraft.inventory.ContainerPlayer")
            || name.equals ("aap"))
            return patchContainerPlayer (basicClass, name.equals ("aap"));
        return basicClass;
    }

    private byte[] patchContainerPlayer (byte[] basicClass, boolean obfuscated)
    {
        ClassNode classNode = new ClassNode ();
        ClassReader classReader = new ClassReader (basicClass);
        classReader.accept (classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator ();
        while (methods.hasNext ())
        {
            MethodNode node = methods.next ();

            if (node.name.equals ("transferStackInSlot") || node.name.equals ("func_82846_b"))
            {
                InsnList list = new InsnList ();

                list.add (new VarInsnNode (Opcodes.ALOAD, 1));
                list.add (new MethodInsnNode (Opcodes.INVOKESTATIC, "pvpmode/PvPUtils", "isShiftClickingBlocked",
                    String.format ("(L%s;)Z",
                        obfuscated ? "yz" : "net/minecraft/entity/player/EntityPlayer"),
                    false));
                LabelNode label1 = new LabelNode ();
                list.add (new JumpInsnNode (Opcodes.IFEQ, label1));
                list.add (new InsnNode (Opcodes.F_SAME));
                list.add (new InsnNode (Opcodes.ACONST_NULL));
                list.add (new InsnNode (Opcodes.ARETURN));
                list.add (label1);

                node.instructions.insertBefore (node.instructions.getFirst (), list);
                System.out.println ("Patched \"transferStackInSlot\" in \"net.minecraft.inventory.ContainerPlayer\"");
                break;
            }
        }

        ClassWriter writer = new ClassWriter (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept (writer);
        return writer.toByteArray ();
    }

}
