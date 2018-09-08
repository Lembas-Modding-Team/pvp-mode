package pvpmode.compatibility;

import java.util.Arrays;

/**
 * A compatibility module loader base class which determines whether a
 * compatibility module can be loaded by an identifier of it's dependencies.
 * Classes who extend it have to determine whether the module can loaded via the
 * identifier.
 *
 * @author CraftedMods
 *
 */
public abstract class IdentifierCompatibilityModuleLoader implements CompatibilityModuleLoader
{

    private final String[] identifiers;
    private Boolean loaded;

    protected IdentifierCompatibilityModuleLoader (String... identifiers)
    {
        this.identifiers = identifiers;
    }

    public String[] getIdentifiers ()
    {
        return Arrays.copyOf (identifiers, identifiers.length);
    }

    @Override
    public boolean canLoad ()
    {
        if (loaded == null)
        {
            loaded = Boolean.TRUE;
            for (String identifier : identifiers)
            {
                if (! (loaded = loaded && isDependencyLoaded (identifier)))
                {
                    break;
                }
            }
        }
        return loaded.booleanValue ();
    }

    /**
     * Returns true if the dependency of this module is present.
     */
    protected abstract boolean isDependencyLoaded (String identifier);

}
