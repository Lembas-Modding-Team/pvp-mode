package pvpmode.internal.common.configuration;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.*;
import java.util.*;

import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.configuration.*;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.Unit;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.utils.PvPServerUtils;
import pvpmode.internal.common.utils.ClassDiscoverer;

/**
 * An internal helper class used for retrieving the auto configuration data with
 * reflection, classpath scanning and so on. It also processes the classes
 * involved into the auto configuration regarding other features, for example
 * the injection of properties. Quiet hacky.
 * 
 * @author CraftedMods
 *
 */
public class AutoConfigurationCreator
{

    private final SimpleLogger logger = PvPCommonUtils.getLogger (AutoConfigurationCreator.class);

    private Map<String, Map<String, ConfigurationPropertyKey<?>>> generatedKeys = new HashMap<> ();

    private Map<Class<?>, ConfigurationPropertyKeyCreator<?>> keyCreators;

    /**
     * Processes the discovered classes and extracts the relevant data and processes
     * these.
     * 
     * @param discoverer
     *            The class discoverer
     * @param timeout
     *            The maximum time the method should wait for the discoverer to
     *            return the results
     */
    public void processClasspath (ClassDiscoverer discoverer, long timeout)
    {
        Map<Class<? extends Annotation>, Map<Class<?>, Set<Class<?>>>> foundClasses = discoverer
            .getDiscoveredClasses (timeout);

        keyCreators = createPropertyCreators (foundClasses);

        processPlainProcessors (foundClasses.get (Process.class));
    }

    public void processConfigurationManager (Class<? extends ConfigurationManager> classToProcess, String pid)
    {
        this.processPlainProcessorCreateKeys (classToProcess, pid);
        this.processPlainProcessorInjectFields (classToProcess, pid);
    }

    /**
     * Returns a map containing the discovered configuration data, assigned to their
     * configuration PID.
     * 
     * @return A map with the configuration data as value and the PID as key
     */
    public Map<String, Map<String, ConfigurationPropertyKey<?>>> getGeneratedKeys ()
    {
        return generatedKeys;
    }

    @SuppressWarnings("unchecked")
    private Map<Class<?>, ConfigurationPropertyKeyCreator<?>> createPropertyCreators (
        Map<Class<? extends Annotation>, Map<Class<?>, Set<Class<?>>>> foundClasses)
    {
        Map<Class<?>, ConfigurationPropertyKeyCreator<?>> keyCreators = new HashMap<> ();

        Collection<ConfigurationPropertyKeyCreator<?>> keyCreatorsCollection = PvPCommonUtils
            .createRegisteredInstances ((Set<Class<? extends ConfigurationPropertyKeyCreator<?>>>) (Set<?>) foundClasses
                .get (Register.class).get (ConfigurationPropertyKeyCreator.class));

        keyCreatorsCollection.forEach (creator ->
        {
            Class<?> propertyKeyType = getGenericInterfaceTypeArgs (creator.getClass (),
                ConfigurationPropertyKeyCreator.class, 0);
            if (propertyKeyType != null)
                keyCreators.put (propertyKeyType, creator);
        });

        logger.debug ("Created %d property key creators", keyCreators.size ());

        return keyCreators;
    }

    private void processPlainProcessors (Map<Class<?>, Set<Class<?>>> foundClasses)
    {
        Collection<Class<?>> classesToProcess = new HashSet<> ();

        Map<String, Collection<Class<?>>> classesToProcessByPid = new HashMap<> ();

        foundClasses.values ().forEach (classesToProcess::addAll);

        for (Class<?> classToProcess : classesToProcess)
        {
            Map<String, String> properties = PvPCommonUtils.getPropertiesFromProcessedClass (classToProcess);

            String manualProcessingValue = properties
                .get (AutoConfigurationConstants.MANUAL_PROCESSING_PROPERTY_KEY);

            if (manualProcessingValue != null && manualProcessingValue.equalsIgnoreCase ("true"))
            {
                continue; // Don't process the class
            }
            else
            {
                // No manual processing, proceed

                if (properties.containsKey (AutoConfigurationConstants.PID_PROPERTY_KEY))
                {
                    String pid = properties.get (AutoConfigurationConstants.PID_PROPERTY_KEY);
                    if (!classesToProcessByPid.containsKey (pid))
                    {
                        classesToProcessByPid.put (pid, new HashSet<> ());
                    }
                    classesToProcessByPid.get (pid).add (classToProcess);

                    if (manualProcessingValue != null && !manualProcessingValue.equalsIgnoreCase ("false"))
                        logger.error (
                            "The value \"%s\" of the manual processing property of the configuration manager \"%s\" is not a boolean value. The property is assumed to be false.",
                            manualProcessingValue, classToProcess.getName ());
                }
            }

        }

        for (String pid : classesToProcessByPid.keySet ())
        {
            for (Class<?> classToProcess : classesToProcessByPid.get (pid))
            {
                this.processPlainProcessorCreateKeys (classToProcess, pid);
            }
        }

        this.logger.debug ("Generated configuration data for %d PIDs", generatedKeys.size ());

        // Replace static final fields marked with @Inject with the respective property,
        // if matches were found
        for (String pid : classesToProcessByPid.keySet ())
        {
            for (Class<?> classToProcess : classesToProcessByPid.get (pid))
            {
                this.processPlainProcessorInjectFields (classToProcess, pid);
            }
        }

    }

    private void processPlainProcessorCreateKeys (Class<?> classToProcess, String pid)
    {
        for (Method method : classToProcess.getDeclaredMethods ())
        {
            ConfigurationPropertyGetter configPropertyAnnotation = method
                .getAnnotation (ConfigurationPropertyGetter.class);

            if (configPropertyAnnotation != null)
            {
                String internalName = configPropertyAnnotation.internalName ();
                Class<?> returnType = PvPCommonUtils.toWrapper (method.getReturnType ());
                Object defaultValue = getDefaultValue (classToProcess, returnType, method);
                ConfigurationPropertyKeyCreator<?> creator = getMatchingKeyCreator (keyCreators, returnType);

                if (creator != null)
                {
                    ConfigurationPropertyKey<?> key = createKey (internalName,
                        returnType, configPropertyAnnotation.category (),
                        configPropertyAnnotation.unit (), defaultValue, creator, method);
                    if (!generatedKeys.containsKey (pid))
                    {
                        generatedKeys.put (pid, new HashMap<> ());
                    }
                    generatedKeys.get (pid).put (key.getInternalName (), key);
                }
                else
                {
                    this.logger.warning (
                        "No matching property key creator was found for the key \"%s\" with the type \"%s\" registered under \"%s\"",
                        internalName, returnType.getName (), pid);
                }

            }
        }
    }

    private void processPlainProcessorInjectFields (Class<?> classToProcess, String pid)
    {
        for (Field field : classToProcess.getDeclaredFields ())
        {
            Class<?> fieldType = field.getType ();
            if (ConfigurationPropertyKey.class.isAssignableFrom (fieldType)
                && Modifier.isStatic (field.getModifiers ()))
            {
                Inject injectAnnotation = field.getAnnotation (Inject.class);
                if (injectAnnotation != null)
                {
                    String propertyName = injectAnnotation.name ().equals ("") ? field.getName ().toLowerCase ()
                        : injectAnnotation.name ();

                    ConfigurationPropertyKey<?> propertyInstance = generatedKeys.get (pid)
                        .get (propertyName);

                    if (propertyInstance != null)
                    {
                        if (fieldType.isAssignableFrom (propertyInstance.getClass ()))
                        {
                            try
                            {
                                field.setAccessible (true);

                                Field modifiersField = Field.class.getDeclaredField ("modifiers");
                                modifiersField.setAccessible (true);
                                modifiersField.setInt (field, field.getModifiers () & ~Modifier.FINAL);

                                field.set (null, propertyInstance);

                                logger.debug (
                                    "Injected the property key \"%s\" into the field \"%s\" in \"%s\", with the PID \"%s\"",
                                    propertyInstance.getInternalName (), field.getName (),
                                    classToProcess.getName (), pid);
                            }
                            catch (Exception e)
                            {
                                this.logger.errorThrowable (
                                    "An unexpected error occurred while trying to inject the respective property key into the field \"%s\" in \"%s\" registered under the PID \"%s\"",
                                    e,
                                    field.getName (), classToProcess, pid);
                            }

                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T, U extends T> U getMatchingKeyCreator (Map<Class<?>, ConfigurationPropertyKeyCreator<?>> keyCreators,
        Class<T> type)
    {
        if (keyCreators.containsKey (type))
            return (U) keyCreators.get (type);
        else
        {
            for (Class<?> key : keyCreators.keySet ())
            {
                if (key.isAssignableFrom (type))
                    return (U) keyCreators.get (key);
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ConfigurationPropertyKey<?> createKey (String processedPropertyName, Class<T> valueType,
        String category, Unit unit, Object defaultValue, ConfigurationPropertyKeyCreator<?> creator, Method method)
    {
        ConfigurationPropertyKey<?> createdKey = ((ConfigurationPropertyKeyCreator<T>) creator).create (
            processedPropertyName, valueType, category, unit,
            (T) defaultValue, method);
        OnPropertyKeyCreatedEvent event = new OnPropertyKeyCreatedEvent (createdKey);
        return PvPServerUtils.postEventAndGetResult (event, event::getResultKey);
    }

    /*
     * Returns the type argument with the specified index assigned to the specified
     * interface of the specified class or null, if it couldn't be determined. If
     * the specified class is annotated with @Register and contains type
     * informations, these will be used.
     */
    private Class<?> getGenericInterfaceTypeArgs (Class<?> clazz, Class<?> interfaceClass, int index)
    {
        Register register = clazz.getAnnotation (Register.class);
        if (register != null)
        {
            Map<String, String> properties = PvPCommonUtils.getPropertiesFromRegisteredClass (clazz);
            if (properties.containsKey (AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY))
            {
                String type = properties.get (AutoConfigurationConstants.PROPERTY_GENERATOR_TYPE_PROPERTY_KEY);
                try
                {
                    return Class.forName (type);
                }
                catch (ClassNotFoundException e)
                {
                    this.logger.errorThrowable (
                        "The class \"%s\" had the property generator type \"%s\" specified in it's register annotation, but that type doesn't exist at runtime",
                        e, clazz.getName (), type);
                }
            }
        }
        for (Type type : clazz.getGenericInterfaces ())
        {
            if (type instanceof ParameterizedType)
            {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType ().getTypeName ().equals (
                    interfaceClass.getName ()))
                {
                    Class<?> typeClazz = typeToClass (parameterizedType.getActualTypeArguments ()[index]);
                    if (typeClazz != null)
                        return typeClazz;
                }
            }
        }
        return null;
    }

    private Class<?> typeToClass (Type type)
    {
        if (type instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) type)
                .getRawType ();
        else if (type instanceof Class<?>)
            return (Class<?>) type;
        return null;
    }

    @SuppressWarnings("unchecked")
    private <R, T> T getDefaultValue (Class<R> handlerClass, Class<T> valueType, Method method)
    {
        if (method.isDefault ())
        {
            try
            {
                R proxyInstance = (R) Proxy.newProxyInstance (
                    Thread.currentThread ().getContextClassLoader (),
                    new Class[]
                    {handlerClass},
                    (proxy, function, args) ->
                    {
                        Constructor<Lookup> constructor = Lookup.class
                            .getDeclaredConstructor (Class.class);
                        constructor.setAccessible (true);
                        return constructor.newInstance (handlerClass)
                            .in (handlerClass)
                            .unreflectSpecial (function, handlerClass)
                            .bindTo (proxy)
                            .invokeWithArguments ();
                    });

                return (T) method.invoke (proxyInstance);
            }
            catch (Throwable e)
            {
                this.logger.errorThrowable ("Couldn't get the default value of the method \"%s\" of the class \"%s\"",
                    e, method.getName (), handlerClass.getName ());
            }
        }
        return null;
    }

    /**
     * A configuration property name mapper which handles the method names. It
     * should be the first mapper called. The same can also be reversed.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PRIORITY_PROPERTY_KEY + "=100000")
    public static class MethodNameMapper implements ConfigurationPropertyNameMapper
    {

        @Override
        public String toInternalName (String definedName)
        {
            return removeMethodPrefix (definedName);
        }

        @Override
        public String toDisplayName (String internalName)
        {
            return internalName;
        }

        private String removeMethodPrefix (String definedName)
        {
            if (definedName.startsWith ("is"))
                return definedName.replaceFirst ("is", "");
            else if (definedName.startsWith ("are"))
                return definedName.replaceFirst ("are", "");
            else if (definedName.startsWith ("get"))
                return definedName.replaceFirst ("get", "");
            else return definedName;
        }
    }

    /**
     * A configuration property name mapper which handles the processed method names
     * and tries to detect the word in it, which then will be separated with an
     * underscore '_'. The same can also be reversed.
     * 
     * @author CraftedMods
     *
     */
    @Register(properties = AutoConfigurationConstants.PRIORITY_PROPERTY_KEY + "=50000")
    public static class SplittingMapper implements ConfigurationPropertyNameMapper
    {

        @Override
        public String toInternalName (String definedName)
        {
            StringBuilder builder = new StringBuilder ();

            for (int i = 0; i < definedName.length (); i++)
            {
                char c = definedName.charAt (i);

                if (Character.isUpperCase (c) && i != 0)
                {
                    builder.append ("_" + c);
                }
                else
                {
                    builder.append (c);
                }
            }
            return builder.toString ();
        }

        @Override
        public String toDisplayName (String internalName)
        {
            StringBuilder builder = new StringBuilder (internalName.length ());

            boolean uppercaseNext = true;

            for (int i = 0; i < internalName.length (); i++)
            {
                char c = internalName.charAt (i);

                if (uppercaseNext)
                {
                    c = Character.toUpperCase (c);
                    uppercaseNext = false;
                }

                if (c == '_')
                {
                    c = ' ';
                    uppercaseNext = true;
                }
                builder.append (c);
            }
            return builder.toString ();
        }

    }

}
