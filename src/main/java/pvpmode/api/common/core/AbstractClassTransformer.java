package pvpmode.api.common.core;

import java.util.Iterator;
import java.util.function.Predicate;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;
import pvpmode.api.common.SimpleLogger;
import pvpmode.internal.common.core.PvPModeCore;

/**
 * An abstract class which class transformers of the PvP Mode Mod can use.
 * 
 * @author CraftedMods
 *
 */
public abstract class AbstractClassTransformer implements IClassTransformer
{

    protected PvPModeCore coremodInstance = PvPModeCore.getInstance ();
    protected SimpleLogger logger = coremodInstance.getLogger ();

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
    protected byte[] patchMethod (byte[] basicClass, Predicate<MethodNode> methodProcessor)
    {
        ClassNode classNode = new ClassNode ();
        ClassReader classReader = new ClassReader (basicClass);
        classReader.accept (classNode, 0);

        Iterator<MethodNode> methods = classNode.methods.iterator ();
        while (methods.hasNext ())
        {
            if (!methodProcessor.test (methods.next ()))
            {
                break;
            }
        }

        ClassWriter writer = new ClassWriter (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept (writer);
        return writer.toByteArray ();
    }

    protected final boolean isObfuscatedEnvironment ()
    {
        return coremodInstance.isObfuscatedEnvironment ();
    }

}
