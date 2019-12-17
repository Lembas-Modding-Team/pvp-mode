package pvpmode.api.common.compatibility;

import cpw.mods.fml.relauncher.Side;

/**
 * The compatibility module loader is the bridge between PvPMode and the
 * compatibility module. PvPMode cannot load the module directly because it can
 * contain references to code which is not present on runtime. Because of this
 * there are compatibility module loaders, which first determine whether the
 * referenced module can be loaded - and only if yes, PvPMode will try to load
 * it.<br>
 * There must not be any compile time dependencies to the referenced module or
 * other mods or libaries.
 *
 * @author CraftedMods
 *
 */
public interface CompatibilityModuleLoader
{
    /**
     * Returns a human-readable name of the referenced compatibility module
     */
    public String getModuleName ();

    /**
     * Returns a short, internal and unlocalized name of the referenced module
     */
    public String getInternalModuleName ();

    /**
     * Returns the class name of the referenced compatibility module
     */
    public String getCompatibilityModuleClassName (Side side);

    /**
     * Determines whether the referenced compatibility can/shall be loaded. This
     * function won't be called before the referenced loading point
     * ({@link CompatibilityModuleLoader#getLoadingPoint()}) has been reached.
     */
    public boolean canLoad ();

    /**
     * The phase when the framework will try to load the referenced compatibility
     * module.
     */
    public default EnumCompatibilityModuleLoadingPoint getLoadingPoint ()
    {
        return EnumCompatibilityModuleLoadingPoint.INIT;
    }

    /**
     * This function will be invoked when the referenced compatibility module should
     * be loaded - before it will be instantiated and loaded, but after
     * {@link CompatibilityModuleLoader#canLoad()} has been invoked.
     */
    public void onPreLoad ();

}
