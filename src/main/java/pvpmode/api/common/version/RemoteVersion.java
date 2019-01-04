package pvpmode.api.common.version;

import java.net.URL;

/**
 * A class containing the data retrieved from the remote version source. It must
 * contain the semantic version, and optionally it can contain a download URL
 * for the new version and the URL of the changelog.
 * 
 * @author CraftedMods
 *
 */
public class RemoteVersion
{

    private final SemanticVersion remoteVersion;
    private final URL changelogURL;
    private final URL downloadURL;

    public RemoteVersion (SemanticVersion remoteVersion, URL downloadURL, URL changelogURL)
    {
        this.remoteVersion = remoteVersion;
        this.downloadURL = downloadURL;
        this.changelogURL = changelogURL;
    }

    public SemanticVersion getRemoteVersion ()
    {
        return this.remoteVersion;
    }

    public URL getDownloadURL ()
    {
        return this.downloadURL;
    }

    public URL getChangelogURL ()
    {
        return this.changelogURL;
    }

}