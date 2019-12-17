package pvpmode.modules.lotr.api.common;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;

import lotr.common.fac.LOTRFaction;
import lotr.common.fac.LOTRFaction.FactionType;

/**
 * A utility class for the LOTR Mod.
 * 
 * @author CraftedMods
 *
 */
public class LOTRCommonUtils
{

    /**
     * Returns a string list of all factions of the specified type.
     * 
     * @param type
     *            The faction type
     * @return The matching factions
     */
    public static Collection<String> getFactionsOfType (FactionType type)
    {
        return getFactionsAsStringCollection (LOTRFaction.getAllOfType (type));
    }

    /**
     * Returns the supplied faction list as a string list, by converting the
     * supplied faction to it's string identifier.
     * 
     * @param factions
     *            The faction list
     * @return The string list
     */
    public static Collection<String> getFactionsAsStringCollection (Collection<LOTRFaction> factions)
    {
        return factions.stream ().map (faction -> faction.name ())
            .collect (Collectors.toList ());
    }

    /**
     * Evaluates all factions referenced directly or indirectly via the specified
     * placeholder, and returns a collection of their string representations.
     * 
     * @param placeholders
     *            A complete map with all placeholders and their contents
     * @param placeholder
     *            The placeholder to evaluate
     * @return The referenced factions
     */
    public static Set<String> getAllFactionsOfPlaceholder (Multimap<String, String> placeholders,
        String placeholder)
    {
        Set<String> ret = new HashSet<> ();

        if (LOTRFaction.forName (placeholder) != null
            || placeholder.equals (LOTRCommonConstants.FACTION_ENTRY_WILDCARD))
        {
            ret.add (placeholder);
            return ret;
        }

        Collection<String> tmpReferences = new HashSet<> (Arrays.asList (placeholder));

        Collection<String> tmpReferences2 = new HashSet<> ();

        while (!tmpReferences.isEmpty ())
        {
            Iterator<String> tmpReferencesIterator = tmpReferences.iterator ();
            while (tmpReferencesIterator.hasNext ())
            {
                String tmpReference = tmpReferencesIterator.next ();

                if (placeholders.containsKey (tmpReference))
                {
                    Collection<String> references = placeholders.get (tmpReference);

                    for (String reference : references)
                    {
                        if (LOTRFaction.forName (reference) != null)
                        {
                            ret.add (reference);
                        }
                        else
                        {
                            tmpReferences2.add (reference);
                        }
                    }

                }
                tmpReferencesIterator.remove ();
            }
            tmpReferences.addAll (tmpReferences2);
            tmpReferences2.clear ();
        }

        return ret;
    }

}
