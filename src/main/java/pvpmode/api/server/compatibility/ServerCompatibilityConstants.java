package pvpmode.api.server.compatibility;

import net.minecraftforge.common.util.EnumHelper;
import pvpmode.api.common.compatibility.EnumCompatibilityModuleLoadingPoint;

public interface ServerCompatibilityConstants
{

    public static final EnumCompatibilityModuleLoadingPoint SERVER_STARTING_LOADING_POINT = EnumHelper
        .addEnum (new Class[][]
        {
            {EnumCompatibilityModuleLoadingPoint.class}}, EnumCompatibilityModuleLoadingPoint.class, "SERVER_STARTING");

}
