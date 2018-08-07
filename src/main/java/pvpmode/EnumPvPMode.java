package pvpmode;

public enum EnumPvPMode
{

    ON, OFF;

    public static EnumPvPMode fromBoolean (boolean value)
    {
        return value ? EnumPvPMode.ON : EnumPvPMode.OFF;
    }

    public boolean toBoolean ()
    {
        return this == EnumPvPMode.ON;
    }

}
