package pvpmode.internal.common.utils;

import java.lang.annotation.Annotation;
import java.util.*;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import pvpmode.api.common.SimpleLogger;

/**
 * A scanner which scans the classpath for classes annotated with the registered
 * annotations, and makes when accessible, sorted by the interfaces they
 * implement.
 * 
 * @author CraftedMods
 *
 */
public class ClassDiscoverer
{

    private final SimpleLogger logger;

    private Set<Class<? extends Annotation>> registeredAnnotations = new HashSet<> ();
    private Map<Class<? extends Annotation>, Map<String, Set<String>>> discoveredClasses = new HashMap<> ();
    private Set<String> interfaces = new HashSet<> ();

    private Thread discovererThread;

    private boolean canRegister = true;

    public ClassDiscoverer (SimpleLogger logger)
    {
        this.logger = logger;
    }

    /**
     * Registers an annotation, classes with the annotation will be included into
     * the scan. If the annotation class is already registered, the method returns
     * false too.
     * 
     * @param annotationClass
     *            The annotation class
     * @return Whether the annotation could be registered
     */
    public boolean registerClassToDiscover (Class<? extends Annotation> annotationClass)
    {
        if (canRegister)
        {
            if (!discoveredClasses.containsKey (annotationClass))
            {
                discoveredClasses.put (annotationClass, new HashMap<> ());
            }

            return this.registeredAnnotations.add (annotationClass);
        }
        return false;
    }

    /**
     * Scans the classpath in a separate thread asynchronously. New annotation
     * classes cannot be registered.
     */
    public void discoverClassesAsync ()
    {
        canRegister = false;
        discovererThread = new Thread ( () ->
        {
            long start = System.currentTimeMillis ();
            FastClasspathScanner scanner = new FastClasspathScanner ();

            ScanResult result = scanner.scan (Runtime.getRuntime ().availableProcessors ());

            for (Class<? extends Annotation> annotationClass : registeredAnnotations)
            {
                Set<String> matchingClassesByAnnotation = new HashSet<> (
                    result.getNamesOfClassesWithAnnotation (annotationClass));

                Set<String> matchingClassesCopy = new HashSet<> (matchingClassesByAnnotation);

                for (String interfaceClass : result.getNamesOfAllInterfaceClasses ())
                {
                    Sets.intersection (new HashSet<> (result.getNamesOfClassesImplementing (interfaceClass)),
                        matchingClassesByAnnotation).forEach (clazz ->
                        {
                            if (!discoveredClasses.get (annotationClass).containsKey (interfaceClass))
                                discoveredClasses.get (annotationClass).put (interfaceClass, new HashSet<> ());

                            discoveredClasses.get (annotationClass).get (interfaceClass)
                                .add (clazz);

                            interfaces.add (interfaceClass);
                            if (result.getNamesOfAllInterfaceClasses ().contains (clazz))
                            {
                                interfaces.add (clazz);
                            }

                            matchingClassesCopy.remove (clazz);
                        });
                }

                for (String interfaceClass : matchingClassesCopy)
                {

                    if (!discoveredClasses.get (annotationClass).containsKey (null))
                        discoveredClasses.get (annotationClass).put (null, new HashSet<> ());

                    discoveredClasses.get (annotationClass).get (null)
                        .add (interfaceClass);
                }
            }

            logger.info ("Scanned the classpath in " + (System.currentTimeMillis () - start) + " milliseconds");
        });
        discovererThread.start ();
    }

    private void waitForDiscoverer (long timeout)
    {
        if (discovererThread.isAlive ())
        {
            try
            {
                discovererThread.join (timeout);
            }
            catch (InterruptedException e)
            {
                logger.errorThrowable ("The class discoverer thread was interrupted", e);
            }
        }
    }

    /**
     * Returns the names of the discovered classes, sorted by the annotation, and
     * the interfaces they implement. This function is useful if only class names
     * have to determined, but the class shouldn't be actually loaded.
     * 
     * @param timeout
     *            The maximum timeout the function should wait for the discoverer to
     *            complete searching
     * @return The discovered class names
     */
    public Map<Class<? extends Annotation>, Map<String, Set<String>>> getDiscoveredClassNames (
        long timeout)
    {
        waitForDiscoverer (timeout);
        return discoveredClasses;
    }

    private Map<String, Class<?>> loadInterfaces ()
    {
        Map<String, Class<?>> ret = new HashMap<> ();

        interfaces.forEach (interfaceName ->
        {
            try
            {
                ret.put (interfaceName, Loader.instance ().getModClassLoader ().loadClass (interfaceName));
            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    ret.put (interfaceName, Class.forName (interfaceName));
                }
                catch (ClassNotFoundException e2)
                {
                    this.logger.errorThrowable ("Coudn't load the discovered interface \"%s\"", e, interfaceName);
                }
            }
        });

        return ret;
    }

    /**
     * Returns the discovered classes, sorted by the annotation, and the interfaces
     * they implement. All discovered classes will be loaded via
     * {@link Loader#getModClassLoader()}, if this fails, maybe with
     * {@link Class#forName(String)}
     * 
     * @param timeout
     *            The maximum timeout the function should wait for the discoverer to
     *            complete searching
     * @return The discovered classes
     */
    public Map<Class<? extends Annotation>, Map<Class<?>, Set<Class<?>>>> getDiscoveredClasses (
        long timeout)
    {
        waitForDiscoverer (timeout);
        Map<String, Class<?>> interfaces = this.loadInterfaces ();

        Map<Class<? extends Annotation>, Map<Class<?>, Set<Class<?>>>> ret = new HashMap<> ();
        discoveredClasses.forEach ( (annotation, value) ->
        {
            ret.put (annotation, new HashMap<> ());
            value.forEach ( (interfaceKey, value2) ->
            {
                if (interfaceKey == null || interfaces.containsKey (interfaceKey))
                {
                    ret.get (annotation).put (interfaceKey == null ? null : interfaces.get (interfaceKey),
                        new HashSet<> ());

                    for (String classname : value2)
                    {
                        try
                        {
                            ret.get (annotation).get (interfaceKey == null ? null : interfaces.get (interfaceKey))
                                .add ( (Loader.instance ().getModClassLoader ().loadClass (classname)));
                        }
                        catch (ClassNotFoundException e)
                        {
                            this.logger.errorThrowable ("Couldn't load the discovered class \"%s\"", e, classname);
                        }
                    }
                }
            });
        });
        return ret;
    }

}