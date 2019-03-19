package pvpmode.internal.common.core;

import pvpmode.api.common.core.AbstractClassTransformer;
import pvpmode.api.common.utils.Register;

/**
 * The class transformer of the PvP Mode Mod, which is executed server- and
 * clientside.
 * 
 * @author CraftedMods
 *
 */
@Register
public class PvPModeCommonClassTransformer extends AbstractClassTransformer
{

    @Override
    public byte[] transform (String name, String transformedName, byte[] basicClass)
    {
        return basicClass;
    }

}
