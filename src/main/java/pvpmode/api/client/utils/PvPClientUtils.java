package pvpmode.api.client.utils;

import pvpmode.api.common.utils.PvPCommonUtils;

public class PvPClientUtils extends PvPCommonUtils
{
    private static Provider provider;

    public static boolean setProvider (Provider provider)
    {
        if (PvPClientUtils.provider == null)
        {
            PvPClientUtils.provider = provider;
            return true;
        }
        return false;
    }

    public interface Provider extends PvPCommonUtils.Provider
    {

    }

}
