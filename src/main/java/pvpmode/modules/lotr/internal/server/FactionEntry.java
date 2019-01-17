package pvpmode.modules.lotr.internal.server;

/**
 * A simple helper class containing an alignment faction specifier and an
 * alignment value.
 *
 * @author CraftedMods
 *
 */
public class FactionEntry
{
    private String factionName;
    private int alignment;

    public FactionEntry (String factionName, int alignment)
    {
        this.factionName = factionName;
        this.alignment = alignment;
    }

    public String getFactionName ()
    {
        return factionName;
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
        result = prime * result + ( (factionName == null) ? 0 : factionName.hashCode ());
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
        if (factionName == null)
        {
            if (other.factionName != null)
                return false;
        }
        else if (!factionName.equals (other.factionName))
            return false;
        return true;
    }

}
