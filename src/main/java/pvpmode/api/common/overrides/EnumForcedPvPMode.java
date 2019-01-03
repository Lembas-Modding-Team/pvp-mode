package pvpmode.api.common.overrides;

import pvpmode.api.common.EnumPvPMode;

/**
 * Contains all possible values for the forced PvP mode.
 *
 * @author CraftedMods
 *
 */
public enum EnumForcedPvPMode
{
    UNDEFINED, ON, OFF;

    public EnumPvPMode toPvPMode ()
    {
        if (this == UNDEFINED)
            throw new RuntimeException ("Cannot refer from UNDEFINED to an actual PvPMode");
        return this == ON ? EnumPvPMode.ON : EnumPvPMode.OFF;
    }

    public static EnumForcedPvPMode fromBoolean (Boolean mode)
    {
        return mode == null ? UNDEFINED : mode ? ON : OFF;
    }
}