package pvpmode.compatibility.modules.lotr;

/**
 * A simple helper class containing a alignment faction specifier and the
 * minimum required alignment to not be hostile to this faction.
 *
 * @author CraftedMods
 *
 */
public class EnemyBiomeFactionEntry
{
    private String factionName;
    private int minAlignment;

    public EnemyBiomeFactionEntry (String factionName, int minAlignment)
    {
        this.factionName = factionName;
        this.minAlignment = minAlignment;
    }

    public String getFactionName ()
    {
        return factionName;
    }

    public int getMinAlignment ()
    {
        return minAlignment;
    }
}
