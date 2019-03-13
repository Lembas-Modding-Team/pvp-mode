package pvpmode.modules.lotr.internal.server;

import java.util.Set;

/**
 * A simple helper class containing an alignment faction specifier, the relevant
 * factions and an alignment value.
 *
 * @author CraftedMods
 *
 */
public class FactionEntry
{
    private final String entryName;
    private final Set<String> involvedFactions;
    private int alignment;

    public FactionEntry (String entryName, Set<String> involvedFactions, int alignment)
    {
        this.entryName = entryName;
        this.involvedFactions = involvedFactions;
        this.alignment = alignment;
    }

    public String getEntryName ()
    {
        return entryName;
    }

    public Set<String> getInvolvedFactions ()
    {
        return involvedFactions;
    }

    public int getAlignment ()
    {
        return alignment;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + alignment;
        result = prime * result + ( (entryName == null) ? 0 : entryName.hashCode ());
        result = prime * result + ( (involvedFactions == null) ? 0 : involvedFactions.hashCode ());
        return result;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass () != obj.getClass ())
            return false;
        FactionEntry other = (FactionEntry) obj;
        if (alignment != other.alignment)
            return false;
        if (entryName == null)
        {
            if (other.entryName != null)
                return false;
        }
        else if (!entryName.equals (other.entryName))
            return false;
        if (involvedFactions == null)
        {
            if (other.involvedFactions != null)
                return false;
        }
        else if (!involvedFactions.equals (other.involvedFactions))
            return false;
        return true;
    }

}
