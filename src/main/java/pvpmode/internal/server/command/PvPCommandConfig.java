package pvpmode.internal.server.command;

import java.util.*;

import org.apache.commons.lang3.tuple.*;

import net.minecraft.command.*;
import net.minecraft.event.*;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.*;
import pvpmode.PvPMode;
import pvpmode.api.common.configuration.*;
import pvpmode.api.common.configuration.ConfigurationManager.Category;
import pvpmode.api.common.configuration.ConfigurationPropertyKey.*;
import pvpmode.api.server.command.ServerCommandConstants;
import pvpmode.api.server.configuration.ServerConfiguration;
import pvpmode.api.server.utils.ServerChatUtils;
import pvpmode.internal.common.configuration.AutoForgeConfigurationManager;
import pvpmode.internal.server.ServerProxy;

public class PvPCommandConfig extends AbstractPvPCommand
{

    private final ServerProxy server;
    private final ServerConfiguration config;

    private Map<String, ConfigurationManager> configurationManagers = new LinkedHashMap<> ();
    private Map<String, String> configurationNameToDisplayName = new HashMap<> ();

    public static final String MAIN_CONFIGURATION_MANAGER = "core";

    public PvPCommandConfig ()
    {
        server = PvPMode.instance.getServerProxy ();
        config = server.getConfiguration ();

        configurationManagers.put (MAIN_CONFIGURATION_MANAGER, config);
        configurationNameToDisplayName.put (MAIN_CONFIGURATION_MANAGER, "Core Configuration");
        server.getCompatibilityManager ().getLoadedModules ().forEach ( (loader, module) ->
        {
            if (module instanceof Configurable)
            {
                Configurable configurationProvider = (Configurable) module;
                configurationManagers.put (loader.getInternalModuleName (), configurationProvider.getConfiguration ());
                configurationNameToDisplayName.put (loader.getInternalModuleName (), loader.getModuleName ());
            }
        });
    }

    @Override
    public String getCommandName ()
    {
        return ServerCommandConstants.PVPCONFIG_COMMAND_NAME;
    }

    @Override
    public String getCommandUsage (ICommandSender sender)
    {
        return ServerCommandConstants.PVPCONFIG_COMMAND_USAGE;
    }

    @Override
    public boolean isAdminCommand ()
    {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel ()
    {
        return 2;
    }

    @Override
    public Collection<Triple<String, String, String>> getShortHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();

        messages.add (Triple.of ("pvpconfig display ", "[configurationName] [categoryName] [propertyName]",
            "Displays the server configuration."));
        messages.add (Triple.of ("pvpconfig reload ", "[configurationName]",
            "Reloads the server configuration."));

        return messages;
    }

    @Override
    public Collection<Triple<String, String, String>> getLongHelpMessages (ICommandSender sender)
    {
        Collection<Triple<String, String, String>> messages = new ArrayList<> ();

        messages.add (Triple.of ("pvpconfig display ", "[configurationName] [categoryName] [propertyName]",
            "Displays most of the server configuration data in a chat GUI. The entries there are clickable, admins can navigate with them through the configurations. One can also navigate directly to a specific configuration property/category by entering the configuration name (\"core\" for the main configuration, the compatibility module name for the others), eventually the category and the property name after the general command."));
        messages.add (Triple.of ("pvpconfig reload ", "[configurationName]",
            "Reloads all configuration data, which means that the settings in the configuration files will replace the ones that are currently loaded in the game. One can also specify a specific configuration that should be reloaded."));

        return messages;
    }

    @Override
    public String getGeneralHelpMessage (ICommandSender sender)
    {
        return "For operators. Allows them to manage, reload and view the server configuration data related to the PvP Mode Mod.";
    }

    @Override
    public void processCommand (ICommandSender sender, String[] args)
    {
        switch (this.requireArguments (sender, args, 0, "display", "reload"))
        {
            case "display":
                this.displayConfiguration (sender, args);
                break;
            case "reload":
                if (args.length == 1)
                {
                    configurationManagers.values ().forEach (ConfigurationManager::load);
                    ServerChatUtils.green (sender,
                        String.format ("Successfully reloaded %d configurations", configurationManagers.size ()));
                }
                else if (args.length == 2)
                {
                    String configurationName = args[1];

                    if (!configurationManagers.containsKey (configurationName))
                        this.invalidConfigurationName (configurationName);

                    configurationManagers.get (configurationName).load ();

                    ServerChatUtils.green (sender,
                        String.format ("Successfully reloaded the configuration \"%s\"",
                            configurationNameToDisplayName.get (configurationName)));
                }
                else
                {
                    this.usageError (sender);
                }
                break;
        }
    }

    private void displayConfiguration (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            this.displayConfigurationManagers (sender);
        }
        else if (args.length > 1)
        {
            String configurationName = args[1];

            if (!configurationManagers.containsKey (configurationName))
                this.invalidConfigurationName (configurationName);

            if (args.length == 2)
            {
                this.displayCategories (sender, configurationName, null);
            }
            else if (args.length == 3)
            {
                this.displayCategories (sender, configurationName, args[2]);
            }
            else if (args.length == 4)
            {
                this.displayConfigurationProperty (sender, configurationName, args);
            }
            else
            {
                this.usageError (sender);
            }
        }
        ServerChatUtils.green (sender, "-------------------------------------");
    }

    private void displayConfigurationManagers (ICommandSender sender)
    {
        this.displayHeader (sender);

        ServerChatUtils.postLocalChatMessages (sender, EnumChatFormatting.DARK_GREEN,
            "Click on an entry to view it's data");
        ServerChatUtils.blue (sender, "", "> Configurations: ");

        configurationManagers.forEach ( (configurationName, manager) ->
        {

            ChatComponentText entry = new ChatComponentText (
                " - " + configurationNameToDisplayName.get (configurationName));

            entry.getChatStyle ().setColor (EnumChatFormatting.GREEN);
            entry.getChatStyle ()
                .setChatClickEvent (new ClickEvent (Action.RUN_COMMAND, "/pvpconfig display " + configurationName));
            entry.getChatStyle ()
                .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText ("Click to show the configuration data of this configuration")));

            sender.addChatMessage (entry);

        });
    }

    private void displayCategories (ICommandSender sender, String configurationName, String currentCategoryName)
    {
        Pair<Category, Map<String, Category>> categoryData = this.computeCurrentCategoryWithChilds (configurationName,
            currentCategoryName);

        if (categoryData == null)
            this.invalidConfigurationCategory (currentCategoryName);

        this.displayNavigationBar (sender, configurationName, categoryData.getKey (), null);

        ServerChatUtils.blue (sender, "", ">> Categor" + (currentCategoryName == null ? "ies" : "y") + ": ");

        displayChildCategories (sender, configurationName, categoryData.getKey (), categoryData.getValue ().values ());
        displayConfigurationEntries (sender, configurationName, categoryData.getKey ());
    }

    private void displayChildCategories (ICommandSender sender, String configurationName,
        Category currentCategory,
        Collection<Category> childCategories)
    {
        if (!childCategories.isEmpty ())
        {
            if (currentCategory != null)
                ServerChatUtils.blue (sender, "", " >>> Subcategories: ");
            for (Category category : childCategories)
            {
                ChatComponentText entry = new ChatComponentText ("  - " + category.getInternalName ());

                entry.getChatStyle ().setColor (EnumChatFormatting.DARK_GREEN);
                entry.getChatStyle ()
                    .setChatClickEvent (
                        new ClickEvent (Action.RUN_COMMAND,
                            "/pvpconfig display " + configurationName + " " + category.getFullName ()));
                entry.getChatStyle ()
                    .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText ("Click to show the content of this category")));

                sender.addChatMessage (entry);
            }
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void displayConfigurationEntries (ICommandSender sender, String configurationName,
        Category currentCategory)
    {
        if (currentCategory != null && !currentCategory.getProperties ().isEmpty ())
        {
            ServerChatUtils.blue (sender, "",
                " >>> Properties: ");

            ConfigurationManager manager = configurationManagers.get (configurationName);
            AutoForgeConfigurationManager forgeManager = null;
            if (manager instanceof AutoForgeConfigurationManager)
            {
                forgeManager = (AutoForgeConfigurationManager) manager;
            }

            for (ConfigurationPropertyKey<?> key : currentCategory.getProperties ().values ())
            {
                String nameString = forgeManager != null ? forgeManager.getDisplayName (key)
                    : server.getAutoConfigMapperManager ().getDisplayName (key.getInternalName ());
                ChatComponentText keyText = new ChatComponentText ("  - " + nameString + ": ");
                keyText.getChatStyle ().setColor (EnumChatFormatting.WHITE);

                keyText.getChatStyle ()
                    .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText ("Click to show more informations about this entry")));
                keyText.getChatStyle ().setChatClickEvent (new ClickEvent (Action.RUN_COMMAND, "/pvpconfig display "
                    + configurationName + " " + currentCategory.getFullName () + " " + key.getInternalName ()));

                String unit = "";

                switch (key.getUnit ())
                {
                    case BLOCKS:
                        unit = " blocks";
                        break;
                    case ITEM_STACKS:
                        unit = " stacks";
                        break;
                    case SECONDS:
                        unit = " s";
                        break;
                    case TICKS:
                        unit = " ticks";
                        break;
                }

                ChatComponentText valueText = new ChatComponentText (
                    configurationManagers.get (configurationName).getProperty (key).toString () + unit);
                valueText.getChatStyle ().setColor (EnumChatFormatting.GRAY);
                sender.addChatMessage (keyText.appendSibling (valueText));
            }
        }
    }

    private void displayConfigurationProperty (ICommandSender sender, String configurationName, String[] args)
    {
        ConfigurationManager manager = this.configurationManagers.get (configurationName);

        String category = args[2];
        String propertyName = args[3];

        if (manager.getPropertyKeys ().containsKey (propertyName))
        {
            ConfigurationPropertyKey<?> key = manager.getPropertyKeys ().get (propertyName);

            if (key.getCategory ().equals (category))
            {
                Category keyCategory = manager.getCategory (key);

                this.displayNavigationBar (sender, configurationName,
                    keyCategory, propertyName);

                AutoForgeConfigurationManager forgeManager = null;
                if (manager instanceof AutoForgeConfigurationManager)
                {
                    forgeManager = (AutoForgeConfigurationManager) manager;
                }

                ServerChatUtils.blue (sender, "", ">> Property: ");

                ServerChatUtils.postLocalChatMessage (sender, " Name: ",
                    forgeManager != null ? forgeManager.getDisplayName (key)
                        : key.getInternalName (),
                    EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                ServerChatUtils.postLocalChatMessage (sender, " Value: ",
                    manager.getProperty (key).toString (),
                    EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);

                if (key.getUnit () != Unit.NONE)
                {
                    ServerChatUtils.postLocalChatMessage (sender, " Unit: ",
                        key.getUnit ().toString (),
                        EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                }

                ServerChatUtils.postLocalChatMessage (sender, " Default Value: ",
                    key.getDefaultValue ().toString (),
                    EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);

                if (key instanceof AbstractNumberKey)
                {
                    AbstractNumberKey<?> abstractNumberKey = (AbstractNumberKey<?>) key;

                    ServerChatUtils.postLocalChatMessage (sender, " Minimum Value: ",
                        abstractNumberKey.getMinValue ().toString (),
                        EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                    ServerChatUtils.postLocalChatMessage (sender, " Maximum Value: ",
                        abstractNumberKey.getMaxValue ().toString (),
                        EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                }

                if (key instanceof ValidValuesHolder)
                {
                    ValidValuesHolder<?, ?> validValuesKey = (ValidValuesHolder<?, ?>) key;
                    if (validValuesKey.getValidValues () != null)
                    {
                        ServerChatUtils.postLocalChatMessage (sender, " Valid Values: ",
                            validValuesKey.getValidValues ().toString (),
                            EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                    }
                }

                if (forgeManager != null)
                {
                    ServerChatUtils.postLocalChatMessage (sender, " Comment: ",
                        forgeManager.getComment (key),
                        EnumChatFormatting.WHITE, EnumChatFormatting.GRAY);
                }

            }
            else
            {
                throw new CommandException (
                    String.format ("There's no configuration property with the name \"%s\"", propertyName));
            }
        }
        else
        {
            this.invalidConfigurationCategory (category);
        }
    }

    private Pair<Category, Map<String, Category>> computeCurrentCategoryWithChilds (String configurationName,
        String currentCategoryName)
    {
        Category currentCategory = null;
        Map<String, Category> childCategories = configurationManagers.get (configurationName).getRootCategories ();

        if (currentCategoryName != null)
        {
            String[] baseParts = currentCategoryName.split ("\\" + ConfigurationPropertyKey.CATEGORY_SEPARATOR);

            for (int i = 0; i < baseParts.length; i++)
            {
                String part = baseParts[i];

                if (childCategories.containsKey (part))
                {
                    currentCategory = childCategories.get (part);
                    childCategories = currentCategory.getSubcategories ();
                }
                else
                {
                    return null;
                }
            }
        }

        return Pair.of (currentCategory, childCategories);
    }

    private void displayNavigationBar (ICommandSender sender, String configurationName, Category currentCategory,
        String currentPropertyName)
    {
        this.displayHeader (sender);

        ChatComponentText rootPart = new ChatComponentText ("> ");

        rootPart.getChatStyle ().setColor (EnumChatFormatting.BLUE);
        rootPart.getChatStyle ()
            .setChatClickEvent (new ClickEvent (Action.RUN_COMMAND, "/pvpconfig display"));
        rootPart.getChatStyle ()
            .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText ("Click to show all configurations")));

        ChatComponentText configurationPart = new ChatComponentText (configurationName + ": ");

        configurationPart.getChatStyle ().setColor (EnumChatFormatting.BLUE);
        configurationPart.getChatStyle ()
            .setChatClickEvent (new ClickEvent (Action.RUN_COMMAND, "/pvpconfig display " + configurationName));
        configurationPart.getChatStyle ()
            .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText ("Click to show all root categories of this configuration")));

        rootPart.appendSibling (configurationPart);

        if (currentCategory != null)
        {
            if (!currentCategory.isRootCategory ())
            {
                String parentCategory = currentCategory.getParentCategory ().getFullName ();

                ChatComponentText parentCategoryPart = new ChatComponentText (parentCategory);

                parentCategoryPart.getChatStyle ().setColor (EnumChatFormatting.DARK_GREEN);
                parentCategoryPart.getChatStyle ()
                    .setChatClickEvent (
                        new ClickEvent (Action.RUN_COMMAND,
                            "/pvpconfig display " + configurationName + " "
                                + parentCategory));
                parentCategoryPart.getChatStyle ()
                    .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText ("Click to show the parent category")));

                rootPart.appendSibling (parentCategoryPart);
            }

            ChatComponentText currentCategoryPart = new ChatComponentText (
                (currentCategory.isRootCategory () ? "" : "/")
                    + (currentCategory.getInternalName ()));

            currentCategoryPart.getChatStyle ()
                .setColor (EnumChatFormatting.GREEN);
            currentCategoryPart.getChatStyle ()
                .setChatClickEvent (
                    new ClickEvent (Action.RUN_COMMAND,
                        "/pvpconfig display " + configurationName + " "
                            + (currentCategory != null ? currentCategory.getFullName () : "")));
            currentCategoryPart.getChatStyle ()
                .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                    new ChatComponentText (currentPropertyName == null ? "Click to redisplay this category"
                        : "Click to show the containing category")));

            if (currentPropertyName != null)
            {
                ChatComponentText currentPropertyPart = new ChatComponentText ("/" + EnumChatFormatting.ITALIC + "#");

                currentPropertyPart.getChatStyle ()
                    .setColor (EnumChatFormatting.GOLD);
                currentPropertyPart.getChatStyle ()
                    .setChatClickEvent (new ClickEvent (ClickEvent.Action.RUN_COMMAND,
                        "/pvpconfig display " + configurationName + " " + currentCategory.getFullName () + " "
                            + currentPropertyName));
                currentPropertyPart.getChatStyle ()
                    .setChatHoverEvent (new HoverEvent (HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText ("Click to redisplay this property")));

                currentCategoryPart.appendSibling (currentPropertyPart);
            }

            rootPart.appendSibling (currentCategoryPart);
        }

        sender.addChatMessage (rootPart);
    }

    private void displayHeader (ICommandSender sender)
    {
        ServerChatUtils.green (sender, "-------- PvP Mode Configuration --------");
    }

    protected void invalidConfigurationCategory (String category)
    {
        throw new CommandException (
            String.format ("The configuration category with the name \"%s\" doesn't exist",
                category));
    }

    protected void invalidConfigurationName (String configurationName)
    {
        throw new CommandException (
            String.format ("The configuration with the name \"%s\" doesn't exist", configurationName));
    }

    @Override
    public List<?> addTabCompletionOptions (ICommandSender sender, String[] args)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord (args, "display", "reload");
        }
        else if (args.length == 2)
        {
            if (args[0].equals ("display") || args[0].equals ("reload"))
                return getListOfStringsMatchingLastWord (args,
                    this.configurationManagers.keySet ().toArray (new String[configurationManagers.size ()]));
        }
        else if (args.length == 3)
        {
            String configurationName = args[1];

            if (configurationManagers.containsKey (configurationName))
            {
                ConfigurationManager manager = configurationManagers.get (configurationName);

                Set<String> categories = new HashSet<> ();

                Collection<Category> categoriesSet = new HashSet<> (manager.getRootCategories ().values ());

                while (!categoriesSet.isEmpty ())
                {
                    Collection<Category> childsSet = new HashSet<> ();

                    for (Category value : categoriesSet)
                    {
                        categories.add (value.getFullName ());
                        childsSet.addAll (value.getSubcategories ().values ());
                    }

                    categoriesSet.clear ();
                    categoriesSet.addAll (childsSet);
                }

                return getListOfStringsMatchingLastWord (args,
                    categories.toArray (new String[categories.size ()]));
            }
        }
        else if (args.length == 4)
        {
            String configurationName = args[1];
            String categoryName = args[2];

            if (configurationManagers.containsKey (configurationName))
            {
                Pair<Category, Map<String, Category>> result = this.computeCurrentCategoryWithChilds (
                    configurationName,
                    categoryName);

                if (result != null)
                {
                    Category category = result.getKey ();
                    return getListOfStringsMatchingLastWord (args,
                        category.getProperties ().keySet ()
                            .toArray (new String[category.getProperties ().size ()]));
                }
            }
        }
        return null;
    }
}
