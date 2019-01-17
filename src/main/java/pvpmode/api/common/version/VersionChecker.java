package pvpmode.api.common.version;

import org.apache.commons.lang3.tuple.Pair;

/**
 * A generic interface for a version checker.
 * 
 * @author CraftedMods
 *
 */
public interface VersionChecker
{
    /**
     * Retrieves the remote version and compares it with the supplied, local
     * version. The returned {@link Pair} contains the retrieved remote version as
     * key and the comparison with the local version as value. If no remote version
     * was found, null is returned as the key.
     * 
     * @return The remote version and the comparison
     */
    public Pair<RemoteVersion, EnumVersionComparison> checkVersion (SemanticVersion localVersion);
}