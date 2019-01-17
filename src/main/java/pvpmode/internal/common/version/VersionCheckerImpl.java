package pvpmode.internal.common.version;

import java.io.*;
import java.net.*;

import org.apache.commons.lang3.tuple.Pair;

import pvpmode.PvPMode;
import pvpmode.api.common.version.*;

public class VersionCheckerImpl implements VersionChecker
{

    private String versionFileURL;

    public VersionCheckerImpl (String versionFileURL)
    {
        this.versionFileURL = versionFileURL;
    }

    public Pair<RemoteVersion, EnumVersionComparison> checkVersion (SemanticVersion localVersion)
    {
        RemoteVersion remoteVersion = null;
        if (this.ping ())
        {
            try
            {
                remoteVersion = this.parseVersionFile (this.downloadVersionFile ());
            }
            catch (IOException e)
            {
                PvPMode.proxy.getLogger ().errorThrowable (
                    "Couldn't download the version file \"%s\"", e, this.versionFileURL.toString ());
            }
            catch (Exception e)
            {
                PvPMode.proxy.getLogger ().errorThrowable (
                    "Couldn't parse the contents of the version file \"%s\"", e,
                    this.versionFileURL.toString ());
            }
        }
        return Pair.of (remoteVersion, this.compareRemoteVersion (localVersion,
            remoteVersion != null ? remoteVersion.getRemoteVersion () : null));
    }

    private boolean ping ()
    {
        if (this.versionFileURL != null)
        {
            try
            {
                URLConnection conn = new URL (this.versionFileURL).openConnection ();
                conn.setConnectTimeout (2000);
                conn.connect ();
                return true;
            }
            catch (MalformedURLException e)
            {
                PvPMode.proxy.getLogger ()
                    .error ("The URL of the version file \"%s\" isn't valid", this.versionFileURL);
            }
            catch (IOException e)
            {
                PvPMode.proxy.getLogger ().errorThrowable (
                    "Cannot connect to the version file \"%s\"", e, this.versionFileURL.toString ());
            }
        }
        return false;
    }

    private String downloadVersionFile () throws IOException
    {
        try (InputStream stream = new URL (versionFileURL).openStream ();
            InputStreamReader bridge = new InputStreamReader (stream);
            BufferedReader reader = new BufferedReader (bridge))
        {
            return reader.readLine ();
        }
    }

    private RemoteVersion parseVersionFile (String versionString) throws MalformedURLException
    {
        if (versionString != null)
        {

            SemanticVersion remoteVersion = null;
            URL downloadURL = null;
            URL changelogURL = null;

            String[] parts = versionString.split ("\\|");

            remoteVersion = SemanticVersion.of (parts[0]);
            if (parts.length >= 1 && !parts[1].trim ().isEmpty ())
            {
                downloadURL = new URL (parts[1]);
            }
            if (parts.length >= 2 && !parts[2].trim ().isEmpty ())
            {
                changelogURL = new URL (parts[2]);
            }

            return new RemoteVersion (remoteVersion, downloadURL, changelogURL);
        }
        return null;
    }

    private EnumVersionComparison compareRemoteVersion (SemanticVersion localVersion, SemanticVersion remoteVersion)
    {
        int comp = remoteVersion != null ? localVersion.compareTo (remoteVersion) : 0;
        if (comp == 0)
            return EnumVersionComparison.CURRENT;
        else if (comp < 0)
            return EnumVersionComparison.NEWER;
        else return EnumVersionComparison.OLDER;
    }
}
