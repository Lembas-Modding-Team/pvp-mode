package pvpmode.api.common.compatibility;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import pvpmode.PvPMode;

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
                if (!isDependencyLoaded (identifier))
                {
                    loaded = Boolean.FALSE;
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

    @Override
    public String getInternalModuleName ()
    {
        return StringUtils.join (identifiers);
    }

    protected boolean isVersionSupported (String identifier, String version)
    {
        return true;
    }

    protected String getIdentifierVersion (String identifier)
    {
        return null;
    }

    @Override
    public void onPreLoad ()
    {
        for (String identifier : this.getIdentifiers ())
        {
            String loadedVersion = getIdentifierVersion (identifier);

            if (loadedVersion != null)
            {
                if (!isVersionSupported (identifier, loadedVersion))
                {
                    PvPMode.proxy.getLogger ().warning (
                        "The compatibility module loader \"%s\" found an unsupported version (%s) of the dependency \"%s\". Trying to load the compatibiliy module anyway.",
                        this.getInternalModuleName (), loadedVersion, identifier);
                }
                else
                {
                    PvPMode.proxy.getLogger ().debug (
                        "The compatibility module loader \"%s\" found an explicitely supported version (%s) of the dependency \"%s\"",
                        this.getInternalModuleName (), loadedVersion, identifier);
                }
            }
        }
    }

}
