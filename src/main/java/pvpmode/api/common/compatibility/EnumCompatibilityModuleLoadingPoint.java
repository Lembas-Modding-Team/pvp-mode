package pvpmode.api.common.compatibility;

import net.minecraftforge.common.util.EnumHelper;

/**
 * The phase when a compatibility module should be loaded. One can add custom
 * ones with {@link EnumHelper}.
 * 
 * @author CraftedMods
 *
 */
public enum EnumCompatibilityModuleLoadingPoint
{
    PRE_INIT, INIT, POST_INIT, LOADING_COMPLETED;

}
