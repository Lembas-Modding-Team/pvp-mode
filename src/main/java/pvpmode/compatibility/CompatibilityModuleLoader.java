package pvpmode.compatibility;

public interface CompatibilityModuleLoader
{
    public String getModuleName ();

    public String getCompatibilityModuleClassName ();

    public boolean canLoad ();

}
