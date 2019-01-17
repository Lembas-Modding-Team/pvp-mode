package pvpmode.internal.common.configuration;

import pvpmode.api.common.configuration.auto.ConfigurationPropertyNameMapper;
import pvpmode.api.common.utils.Register;

/**
 * A class collecting the default configuration property name mappers for the
 * PvP Mode Mod.
 * 
 * @author CraftedMods
 *
 */
public class ConfigurationPropertyNameMappers
{

    @Register
    public static class PvPMapper implements ConfigurationPropertyNameMapper
    {

        @Override
        public String toInternalName (String definedName)
        {
            return definedName.replaceAll ("Pv_P", "PvP");
        }

        @Override
        public String toDisplayName (String internalName)
        {
            return internalName.replaceAll ("Pvp", "PvP");
        }
    }

    @Register
    public static class PvEMapper implements ConfigurationPropertyNameMapper
    {

        @Override
        public String toInternalName (String definedName)
        {
            return definedName.replaceAll ("Pv_E", "PvE");
        }

        @Override
        public String toDisplayName (String internalName)
        {
            return internalName.replaceAll ("Pve", "PvE");
        }

    }

    @Register
    public static class CSVMapper implements ConfigurationPropertyNameMapper
    {

        @Override
        public String toInternalName (String definedName)
        {
            return definedName.replaceAll ("C_S_V", "CSV");
        }

        @Override
        public String toDisplayName (String internalName)
        {
            return internalName.replaceAll ("Csv", "CSV");
        }

    }
}
