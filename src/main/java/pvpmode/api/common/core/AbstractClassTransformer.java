package pvpmode.api.common.core;

import java.util.Iterator;
import java.util.function.*;

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

    protected final PvPModeCore coremodInstance = PvPModeCore.getInstance ();
    protected final SimpleLogger logger = coremodInstance.getLogger ();

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
    protected byte[] patchClass (byte[] basicClass, String classDisplayName, Predicate<MethodNode> methodProcessor)
    {
        logger.debug ("Patching the class \"%s\"...", classDisplayName);
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

        logger.debug ("Finished patching the class \"%s\"", classDisplayName);

        ClassWriter writer = new ClassWriter (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept (writer);
        return writer.toByteArray ();
    }

    protected <T> boolean patchMethod (String methodName, MethodNode methodNode,
        Function<MethodNode, T> preCondition, BiPredicate<MethodNode, T> patcher)
    {
        T conditionResult = preCondition.apply (methodNode);

        boolean patchResult = conditionResult == null ? false : patcher.test (methodNode, conditionResult);

        if (conditionResult == null || !patchResult)
        {
            logger.warning ("Could not patch the method \"%s\" %s", methodName,
                conditionResult == null ? "because the pre-conditions were not satisfied" : "");
        }
        else
        {
            logger.debug ("Patched the method \"%s\"", methodName);
        }

        return patchResult;
    }

    protected final boolean isObfuscatedEnvironment ()
    {
        return coremodInstance.isObfuscatedEnvironment ();
    }

}
