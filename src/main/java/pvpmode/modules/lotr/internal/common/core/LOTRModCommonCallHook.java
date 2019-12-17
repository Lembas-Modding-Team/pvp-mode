package pvpmode.modules.lotr.internal.common.core;

import java.lang.reflect.Field;
import java.util.*;

import cpw.mods.fml.relauncher.IFMLCallHook;
import net.minecraft.launchwrapper.*;
import pvpmode.api.common.utils.Register;
import pvpmode.internal.common.core.PvPModeCore;

@Register
public class LOTRModCommonCallHook implements IFMLCallHook
{

    @Override
    @SuppressWarnings("unchecked")
    public Void call () throws Exception
    {
        try
        {
            Field transformerExceptionField = LaunchClassLoader.class.getDeclaredField ("transformerExceptions");
            transformerExceptionField.setAccessible (true);

            Set<String> set = (Set<String>) transformerExceptionField.get (Launch.classLoader);
            if (set.remove ("lotr"))
            {
                set.add ("lotr.common.core");
                PvPModeCore.getInstance ().getLogger ()
                    .debug (
                        "Removed the transformation exclusion \"lotr\" and replaced it with \"lotr.common.core\"");
            }
        }
        catch (Exception e)
        {
            PvPModeCore.getInstance ().getLogger ()
                .errorThrowable ("Couldn't remove the transformation exclusion \"lotr\"", e);
        }

        return null;
    }

    @Override
    public void injectData (Map<String, Object> data)
    {

    }

}
