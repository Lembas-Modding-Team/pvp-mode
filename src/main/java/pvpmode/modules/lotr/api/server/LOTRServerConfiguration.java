package pvpmode.modules.lotr.api.server;

import java.util.Arrays;

import com.google.common.collect.*;

import lotr.common.fac.LOTRFaction;
import lotr.common.fac.LOTRFaction.FactionType;
import pvpmode.api.common.configuration.ConfigurationManager;
import pvpmode.api.common.configuration.auto.*;
import pvpmode.api.common.utils.Process;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.modules.lotr.api.common.LOTRCommonUtils;

@Process(properties =
{AutoConfigurationConstants.PID_PROPERTY_KEY + "="
    + LOTRServerConfiguration.LOTR_SERVER_CONFIG_PID,
    AutoConfigurationConstants.MANUAL_PROCESSING_PROPERTY_KEY + "=true"})
public interface LOTRServerConfiguration extends ConfigurationManager
{

    public static final String LOTR_SERVER_CONFIG_PID = "lotr-compatibility-server";

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areEnemyBiomeOverridesEnabled ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean isFastTravelingWhilePvPBlocked ()
    {
        return true;
    }

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default boolean areSafeBiomeOverridesEnabled ()
    {
        return false;
    }

    @ConfigurationPropertyGetter(category = ServerConfiguration.SERVER_CATEGORY)
    public default Multimap<String, String> getFactionPlaceholders ()
    {
        Multimap<String, String> placeholders = ArrayListMultimap.create ();

        placeholders.putAll ("ORCS", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_ORC));
        placeholders.putAll ("ELVES", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_ELF));
        placeholders.putAll ("DWARVES", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_DWARF));
        placeholders.putAll ("MEN", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_MAN));
        placeholders.putAll ("TREES", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_TREE));
        placeholders.putAll ("TROLLS", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_TROLL));
        placeholders.putAll ("FREE_PEOPLE", LOTRCommonUtils.getFactionsOfType (FactionType.TYPE_FREE));
        placeholders.putAll ("HARADRIM",
            LOTRCommonUtils.getFactionsAsStringCollection (LOTRFaction.getAllHarad ()));
        placeholders.putAll ("RHUDHRIM",
            LOTRCommonUtils.getFactionsAsStringCollection (LOTRFaction.getAllRhun ()));

        placeholders.putAll ("GOOD",
            LOTRCommonUtils.getFactionsAsStringCollection (
                Arrays.asList (LOTRFaction.BLUE_MOUNTAINS, LOTRFaction.DALE, LOTRFaction.DORWINION, LOTRFaction.DWARF,
                    LOTRFaction.GALADHRIM, LOTRFaction.FANGORN, LOTRFaction.GONDOR, LOTRFaction.HIGH_ELF,
                    LOTRFaction.HOBBIT, LOTRFaction.RANGER_NORTH, LOTRFaction.ROHAN, LOTRFaction.TAUREDAIN,
                    LOTRFaction.WOOD_ELF)));
        placeholders.putAll ("EVIL",
            LOTRCommonUtils.getFactionsAsStringCollection (
                Arrays.asList (LOTRFaction.ANGMAR, LOTRFaction.DOL_GULDUR, LOTRFaction.DUNLAND, LOTRFaction.GUNDABAD,
                    LOTRFaction.HALF_TROLL, LOTRFaction.MORDOR, LOTRFaction.MOREDAIN, LOTRFaction.NEAR_HARAD,
                    LOTRFaction.RHUN, LOTRFaction.URUK_HAI)));

        return placeholders;
    }

}
