package pvpmode.internal.common.core;

import java.util.*;
import java.util.function.Supplier;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import net.minecraft.launchwrapper.IClassTransformer;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.auto.AutoConfigurationConstants;
import pvpmode.api.common.utils.PvPCommonUtils;

/**
 * The class transformer for the auto configuration system. Hacks into the
 * relevant classes to get the auto configuration system to work.
 * 
 * @author CraftedMods
 *
 */
public class AutoConfigurationTransformer implements IClassTransformer
{

    private SimpleLogger logger = PvPCommonUtils.getLogger (AutoConfigurationTransformer.class);

    private Map<String, Map<String, Set<MethodNode>>> processInterfaces = new HashMap<> ();

    private boolean mappersLoaded = false;

    public AutoConfigurationTransformer ()
    {
        if (!mappersLoaded)
        {
            PvPModeCore.autoConfigurationMapperManager
                .processClasspath (PvPModeCore.classDiscoverer, 30000);
            mappersLoaded = true;
        }
    }

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        return patchAutoConfigurationManager (name, basicClass);
    }

    private byte[] patchAutoConfigurationManager (String className, byte[] basicClass)
    {
        ClassNode classNode = new ClassNode ();
        ClassReader classReader = new ClassReader (basicClass);
        classReader.accept (classNode, 0);

        boolean didWrite = false;
        if ( (classNode.access & Opcodes.ACC_INTERFACE) != 0)
        {
            // The node represents an interface, it'll be scanned for configuration property
            // getter annotations
            String pid = getConfigPID (classNode);
            if (pid != null)
            {
                for (MethodNode methodNode : classNode.methods)
                {
                    if (methodNode.visibleAnnotations != null)
                    {
                        for (AnnotationNode annotation : methodNode.visibleAnnotations)
                        {
                            if (annotation.desc
                                .contains ("pvpmode/api/common/configuration/auto/ConfigurationPropertyGetter"))
                            {
                                if ( (methodNode.access & Opcodes.ACC_STATIC) == 0)
                                {
                                    // Set the internal name of the property at the annotation, if not already set

                                    String internalName = this.getInternalName (methodNode);
                                    if (internalName == null || internalName.trim ().equals (""))
                                    {
                                        internalName = PvPModeCore.autoConfigurationMapperManager.getInternalName (
                                            methodNode.name);
                                    }
                                    internalName = internalName.toLowerCase ().replaceAll (" ", "_");
                                    this.setInternalName (methodNode, internalName);
                                    didWrite = true;

                                    if ( (methodNode.access & Opcodes.ACC_ABSTRACT) == 0)
                                    {
                                        // We have a default method, it'll be overridden in any class the implements
                                        // this interface to return the actual property value.

                                        if (!processInterfaces.containsKey (pid))
                                            processInterfaces.put (pid, new HashMap<> ());
                                        if (!processInterfaces.get (pid).containsKey (classNode.name))
                                            processInterfaces.get (pid).put (classNode.name, new HashSet<> ());
                                        processInterfaces.get (pid).get (classNode.name).add (methodNode);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else if ( (classNode.access & Opcodes.ACC_ANNOTATION) == 0)
        {

            String pid = getConfigPID (classNode);

            if (pid != null && processInterfaces.containsKey (pid))
            {
                for (String implementedInterface : classNode.interfaces)
                {
                    // Scan whether the class implements one of the configuration interfaces with
                    // the same PID
                    if (processInterfaces.get (pid).containsKey (implementedInterface))
                    {
                        // Override the default getters, to return the actual property value
                        for (MethodNode toGenerate : processInterfaces.get (pid).get (implementedInterface))
                        {
                            classNode.methods.add (
                                this.createNewGetter (classNode.name, toGenerate, this.getInternalName (toGenerate)));
                            didWrite = true;
                        }
                    }
                }
            }
        }
        if (didWrite)
        {
            ClassWriter writer = new ClassWriter (ClassWriter.COMPUTE_MAXS |
                ClassWriter.COMPUTE_FRAMES);
            classNode.accept (writer);

            logger.debug ("Patched the class \"%s\"", className);

            return writer.toByteArray ();
        }
        else
        {
            return basicClass;
        }
    }

    private MethodNode createNewGetter (String internalClassName, MethodNode defaultMethod,
        String internalPropertyKeyName)
    {
        MethodNode node = new MethodNode (Opcodes.ACC_PUBLIC, defaultMethod.name, defaultMethod.desc,
            defaultMethod.signature,
            defaultMethod.exceptions.toArray (new String[defaultMethod.exceptions.size ()]));

        Type type = Type.getReturnType (defaultMethod.desc);

        LabelNode label0 = new LabelNode ();
        node.instructions.add (label0);
        node.instructions.add (new VarInsnNode (Opcodes.ALOAD, 0));
        node.instructions.add (new VarInsnNode (Opcodes.ALOAD, 0));
        node.instructions.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL,
            internalClassName,
            "getPropertyKeys",
            "()Ljava/util/Map;",
            false));
        node.instructions.add (new LdcInsnNode (internalPropertyKeyName));
        node.instructions.add (new MethodInsnNode (Opcodes.INVOKEINTERFACE,
            "java/util/Map",
            "get",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            true));
        node.instructions
            .add (new TypeInsnNode (Opcodes.CHECKCAST, "pvpmode/api/common/configuration/ConfigurationPropertyKey"));
        node.instructions.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL,
            internalClassName, "getProperty",
            "(Lpvpmode/api/common/configuration/ConfigurationPropertyKey;)Ljava/lang/Object;",
            false));
        this.addCastInstructions (type, node);
        LabelNode label1 = new LabelNode ();
        node.instructions.add (label1);

        return node;
    }

    private void addCastInstructions (Type type, MethodNode method)
    {
        if (type.equals (Type.BOOLEAN_TYPE))
        {
            method.instructions.add (new TypeInsnNode (Opcodes.CHECKCAST, "java/lang/Boolean"));
            method.instructions.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "java/lang/Boolean",
                "booleanValue", "()Z", false));
            method.instructions.add (new InsnNode (Opcodes.IRETURN));
        }
        else if (type.equals (Type.INT_TYPE))
        {
            method.instructions.add (new TypeInsnNode (Opcodes.CHECKCAST, "java/lang/Integer"));
            method.instructions.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "java/lang/Integer",
                "intValue", "()I", false));
            method.instructions.add (new InsnNode (Opcodes.IRETURN));
        }
        else if (type.equals (Type.FLOAT_TYPE))
        {
            method.instructions.add (new TypeInsnNode (Opcodes.CHECKCAST, "java/lang/Float"));
            method.instructions.add (new MethodInsnNode (Opcodes.INVOKEVIRTUAL, "java/lang/Float",
                "floatValue", "()F", false));
            method.instructions.add (new InsnNode (Opcodes.FRETURN));
        }
        else
        {
            method.instructions.add (new TypeInsnNode (Opcodes.CHECKCAST, type.getInternalName ()));
            method.instructions.add (new InsnNode (Opcodes.ARETURN));
        }
    }

    @SuppressWarnings("unchecked")
    private String getConfigPID (ClassNode classNode)
    {
        List<String> properties = (List<String>) getAnnotatedProperty ( () -> classNode.visibleAnnotations,
            "pvpmode/api/common/utils/Process",
            "properties");

        if (properties != null){ return PvPCommonUtils
            .getPropertiesFromArray (properties.toArray (new String[properties.size ()]))
            .get (AutoConfigurationConstants.PID_PROPERTY_KEY); }
        return null;
    }

    private String getInternalName (MethodNode methodNode)
    {
        return (String) getAnnotatedProperty ( () -> methodNode.visibleAnnotations,
            "pvpmode/api/common/configuration/auto/ConfigurationPropertyGetter",
            "internalName");
    }

    private Object getAnnotatedProperty (Supplier<List<AnnotationNode>> holder, String annotationName,
        String propertyKey)
    {
        List<AnnotationNode> visibleAnnotations = holder.get ();
        if (visibleAnnotations != null)
        {
            for (AnnotationNode node : visibleAnnotations)
            {
                if (node.desc.contains (annotationName))
                {
                    boolean enabled = node.values.contains ("enabled")
                        ? (boolean) node.values.get (node.values.indexOf ("enabled") + 1)
                        : true;
                    if (enabled)
                    {
                        if (node.values.contains (propertyKey)){ return node.values
                            .get (node.values.indexOf (propertyKey) + 1); }
                    }
                    break;
                }
            }
        }
        return null;
    }

    private void setAnnotatedProperty (Supplier<List<AnnotationNode>> holder, String annotationName,
        String propertyKey, Object newValue)
    {
        List<AnnotationNode> visibleAnnotations = holder.get ();
        if (visibleAnnotations != null)
        {
            for (AnnotationNode node : visibleAnnotations)
            {
                if (node.desc.contains (annotationName))
                {
                    boolean enabled = node.values.contains ("enabled")
                        ? (boolean) node.values.get (node.values.indexOf ("enabled") + 1)
                        : true;
                    if (enabled)
                    {
                        if (node.values.contains (propertyKey))
                        {
                            node.values.set (node.values.indexOf (propertyKey) + 1, newValue);
                        }
                        else
                        {
                            node.values.add (propertyKey);
                            node.values.add (newValue);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void setInternalName (MethodNode node, String newValue)
    {
        this.setAnnotatedProperty ( () -> node.visibleAnnotations,
            "pvpmode/api/common/configuration/auto/ConfigurationPropertyGetter", "internalName", newValue);
    }

}
