package pvpmode.api.server.command;

public interface ServerCommandConstants
{

    public static final String PVP_COMMAND_NAME = "pvp";
    public static final String PVP_COMMAND_USAGE = "/pvp [cancel|info] OR /pvp spy [on|off]";

    public static final String PVPADMIN_COMMAND_NAME = "pvpadmin";
    public static final String PVPADMIN_COMMAND_USAGE = "/pvpadmin <player> [on|off|default] OR /pvpadmin info <player>";

    public static final String PVPCONFIG_COMMAND_NAME = "pvpconfig";
    public static final String PVPCONFIG_COMMAND_USAGE = "/pvpconfig display";

    public static final String PVPHELP_COMMAND_NAME = "pvphelp";
    public static final String PVPHELP_COMMAND_USAGE = "/pvphelp [commandName]";

    public static final String PVPLIST_COMMAND_NAME = "pvplist";
    public static final String PVPLIST_COMMAND_USAGE = "/pvplist [all] OR /pvplist <maxEntryCount>";

}
