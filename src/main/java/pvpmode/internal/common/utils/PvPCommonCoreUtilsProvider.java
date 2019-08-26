package pvpmode.internal.common.utils;

import org.apache.logging.log4j.LogManager;

import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.utils.PvPCommonCoreUtils;
import pvpmode.internal.common.SimpleLoggerImpl;

public class PvPCommonCoreUtilsProvider implements PvPCommonCoreUtils.Provider
{
    
    @Override
    public SimpleLogger getLogger (String name)
    {
        return new SimpleLoggerImpl (LogManager.getLogger (name));
    }

}
