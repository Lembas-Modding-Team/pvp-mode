package pvpmode.internal.server.compatibility.modules.lotr;

/**
 * A simple helper class containing an alignment faction specifier and an
 * alignment value determining whether an override applies or not.
 *
 * @author CraftedMods
 *
 */
public class BiomeFactionEntry
{
    private String factionName;
    private int alignment;

    public BiomeFactionEntry (String factionName, int alignment)
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
}
