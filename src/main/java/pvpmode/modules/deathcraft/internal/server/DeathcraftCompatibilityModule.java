package pvpmode.modules.deathcraft.internal.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import pvpmode.api.common.SimpleLogger;
import pvpmode.api.common.compatibility.AbstractCompatibilityModule;
import pvpmode.api.common.compatibility.CompatibilityModuleLoader;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent;
import pvpmode.api.server.compatibility.events.PartialItemDropEvent.Drop.Action;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * The compatibility module for deathcraft.<br> Because of the dynamic nature of Bukkit and the way
 * deathcraft was coded, I had to use ugly hacks, including reflection and ASM, to get this to
 * actually work.
 *
 * @author CraftedMods
 */
public class DeathcraftCompatibilityModule extends AbstractCompatibilityModule
{

    /*
     * This method allows us to inject dynamically generated classes into any
     * classloader, but it's not visible by default.
     */
    private Method defineClassMethod;

    @Override
    public void load (CompatibilityModuleLoader loader, Path configurationFolder,
        SimpleLogger logger) throws Exception
    {
        super.load (loader, configurationFolder, logger);

        MinecraftForge.EVENT_BUS.register (this);

        defineClassMethod = ClassLoader.class.getDeclaredMethod ("defineClass",
            String.class, byte[].class, int.class, int.class);
        defineClassMethod.setAccessible (true);
    }

    /*
     * This section contains data shared between the event listeners for the events
     * fired by the partial inventors loss algorithm. As they're all fired in one
     * thread and after another, we can safely store the data here. After the
     * PostDrop event, all fields have to been set to null.
     *
     * <<<<< START >>>>>
     */

    private Boolean canCreateChest = null;

    private Object deathcraftPluginInstance;

    private Class<?> deathcraftClass;

    private Field pvpChestEnabledField;
    private Field pveChestEnabledField;

    private Class<?> deathChestsClass;

    private Method canDropChestAtLocationMethod;
    private Method goodLocMethod;
    private Method goodLocLargeMethod;
    private Method saveChestMethod;

    private Class<?> utilClass;

    private Method infoLogMethod;

    private Player player;

    private int requiredChests;
    private boolean useLargeChest;

    private Block chestLocation;
    private Block chestLocation2;

    private List<net.minecraft.item.ItemStack> drops;

    /*
     * <<<<< END >>>>>
     */

    @SubscribeEvent
    public void onPartialItemPreDrop (PartialItemDropEvent.Pre event)
    {

        /*
         * We'll verify that deathcraft is still enabled and works
         */

        canCreateChest = false; // The chest will only be created if all checks pass
        try
        {
            if (Bukkit.getPluginManager ().isPluginEnabled ("deathcraft"))
            {
                setupReflection ();
                player = Bukkit.getPlayer (event.getPlayer ().getUniqueID ());

                // Various checks
                if ((Boolean) canDropChestAtLocationMethod.invoke (null, player,
                    player.getLocation ()))
                {
                    if (!event.isPvPDeath () ? pveChestEnabledField.getBoolean (null)
                        : pvpChestEnabledField.getBoolean (null))
                    {
                        int foundChests = 0;

                        // Look for chests in the player's inventory
                        for (ItemStack item : player.getInventory ())
                        {
                            if (item != null)
                            {
                                if (item.getType () == Material.CHEST)
                                {
                                    foundChests += item.getAmount ();
                                }
                            }
                        }

                        requiredChests = -1;

                        // Look if the player has the required permissions and optionally required
                        // chests in his inventory
                        if (player.hasPermission ("deathcraft.chest.large.free"))
                        {
                            useLargeChest = true;
                            requiredChests = 0;
                        }
                        else if (player.hasPermission ("deathcraft.chest.large")
                            && foundChests >= 2)
                        {
                            useLargeChest = true;
                            requiredChests = 2;
                        }
                        else if (player.hasPermission ("deathcraft.chest.small.free"))
                        {
                            requiredChests = 0;
                        }
                        else if (player.hasPermission ("deathcraft.chest.small")
                            && foundChests >= 1)
                        {
                            requiredChests = 1;
                        }

                        if (requiredChests
                            != -1)// Only proceed if the player has the required chests in the inventory
                        {
                            chestLocation = player.getWorld ()
                                .getBlockAt (player.getLocation ().getBlockX (),
                                    player.getLocation ().getBlockY (),
                                    player.getLocation ().getBlockZ ());
                            chestLocation2 = null; // The location of the second chest if the chest is large

                            // Test if the first chest can be positioned
                            if ((chestLocation = (Block) goodLocMethod.invoke (null, chestLocation))
                                != null)
                            {
                                // Test if the second chest can be positioned
                                if (useLargeChest
                                    ? (chestLocation2 = (Block) goodLocLargeMethod
                                    .invoke (null, chestLocation)) != null
                                    : true)
                                {
                                    canCreateChest = true; // All checks have passed, we can create the chest
                                    if (drops == null)
                                    {
                                        drops = new ArrayList<> ();
                                    }
                                }
                            }
                            else
                            {
                                infoLogMethod.invoke (null,
                                    "Chest location could not be found for " + player.getName ());
                            }
                        }
                    }
                }
            }
        }
        catch (IllegalAccessException e)
        {
            logger.errorThrowable ("Couldn't access one of the methods or fields of deathcraft", e);
        }
        catch (InvocationTargetException e)
        {
            logger.errorThrowable ("A method of deathcraft threw an exception", e);
        }

        if (canCreateChest != Boolean.TRUE)
        {
            resetData (); // Reset the stored data of the checks didn't pass
        }
    }

    /*
     * Get access to the required fields and methods of deathcraft. This will be
     * done every death because the plugin is loaded in a dynamic environment and
     * might not be present anymore. We cannot keep references to the fields and
     * functions because otherwise this dynamic classloading mechanism won't work
     * anymore.
     */
    private void setupReflection ()
    {
        try
        {
            deathcraftPluginInstance = Bukkit.getPluginManager ().getPlugin ("deathcraft");
            deathcraftClass = deathcraftPluginInstance.getClass ();

            /*
             * Deathcraft contains references to classes which don't have to be present on
             * runtime. If we try to access the fields of classes of deathcraft via
             * reflection, it'll throw an error. Because of that we've to generate these
             * classes dynamically at runtime.
             */
            createMissingClasses (deathcraftClass);

            pvpChestEnabledField = deathcraftClass.getDeclaredField ("PVPChest");
            pvpChestEnabledField.setAccessible (true);

            pveChestEnabledField = deathcraftClass.getDeclaredField ("PVEChest");
            pveChestEnabledField.setAccessible (true);

            ClassLoader loader = deathcraftClass
                .getClassLoader (); // The plugin-specific classloader

            deathChestsClass = ReflectionHelper.getClass (loader, "me.raum.deathcraft.DeathChests");

            createMissingClasses (deathChestsClass);

            canDropChestAtLocationMethod = deathChestsClass
                .getDeclaredMethod ("canDropChestatLocation",
                    Player.class,
                    Location.class);
            canDropChestAtLocationMethod.setAccessible (true);

            goodLocMethod = deathChestsClass.getDeclaredMethod ("goodloc", Block.class);
            goodLocLargeMethod = deathChestsClass.getDeclaredMethod ("goodloclarge", Block.class);

            saveChestMethod = deathChestsClass
                .getDeclaredMethod ("saveChest", Chest.class, Player.class);

            utilClass = ReflectionHelper.getClass (loader, "me.raum.deathcraft.Util");

            createMissingClasses (utilClass);

            infoLogMethod = utilClass.getDeclaredMethod ("info", Object.class);
        }
        catch (NoSuchFieldException e)
        {
            logger.errorThrowable ("Couldn't find a required field of deathcraft", e);
        }
        catch (NoSuchMethodException e)
        {
            logger.errorThrowable ("Couldn't find a required method of deathcraft", e);
        }
        catch (SecurityException e)
        {
            logger.errorThrowable ("Couldn't change the visibility of deathcraft field or methods",
                e);
        }
        catch (IllegalArgumentException e)
        {
            logger.errorThrowable ("A method of deathcraft was used wrongly", e);
        }

    }

    private void createMissingClasses (Class<?> clazz)
    {
        try
        {
            /*
             * This function will throw a NoClassDefFoundError if a field referenced a
             * unknown class. I don't like that I've to do it that way, but it seems to be
             * the only practicable solution here.
             */
            clazz.getDeclaredFields ();
        }
        catch (NoClassDefFoundError e)
        {

            /*
             * Here we're extracting the classname from the error and try to generate and
             * inject the class.
             */
            String bytecodeName = e.getMessage ()
                .substring (1, e.getMessage ().length () - 1); // A name like
            // java/lang/String
            String className = bytecodeName.replaceAll ("/", "."); // A name like java.lang.String

            logger.warning (
                "The class \"%s\" referenced from a field of \"%s\" couldn't be found. We'll try to create it dynamically...",
                className,
                clazz.getName ());

            try
            {
                createClassDynamically (clazz.getClassLoader (), bytecodeName, className);
                logger.info ("Successfully created the class \"%s\" dynamically", className);
            }
            catch (IllegalAccessException e1)
            {
                logger.errorThrowable (
                    "Couldn't access the method \"defineClass\" of the relevant classloader", e);
            }
            catch (IllegalArgumentException e1)
            {
                logger.errorThrowable (
                    "The method \"defineClass\" of the relevant classloader was used wrongly",
                    e);
            }
            catch (InvocationTargetException e1)
            {
                logger.errorThrowable (
                    "The method \"defineClass\" of the relevant classloader threw an exception",
                    e);
            }
            createMissingClasses (clazz); // Run it again until all undefined classes were generated
        }
    }

    /*
     * Here we'll try to generate the missing class via ASM and inject it into the
     * relevant classloader
     */
    private void createClassDynamically (ClassLoader classloader, String bytecodeName,
        String className)
        throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        ClassWriter writer = new ClassWriter (0);
        writer
            .visit (Opcodes.V1_8, Opcodes.ACC_PUBLIC, bytecodeName, null, "java/lang/Object", null);
        writer.visitEnd ();

        byte[] classBytes = writer.toByteArray (); // Create the class and store it in a byte array

        // Inject it into the classloader
        defineClassMethod.invoke (classloader, className, classBytes, 0, classBytes.length);
    }

    @SubscribeEvent
    public void onPartialItemDrop (PartialItemDropEvent.Drop event)
    {
        if (canCreateChest == Boolean.TRUE) // Only proceed if the checks passed
        {
            drops.add (event.getStack ());
            event.setAction (Action.DELETE);
        }
    }

    @SubscribeEvent
    public void onPartialItemPostDrop (PartialItemDropEvent.Post event)
    {
        if (canCreateChest == Boolean.TRUE)
        {
            try
            {
                chestLocation.setType (Material.CHEST); // Place the chest

                if (useLargeChest)
                {
                    chestLocation2.setType (Material.CHEST); // Place the second chest
                }

                Chest chest = (Chest) chestLocation.getState ();
                Chest chest2 = null;

                if (useLargeChest)
                {
                    chest2 = (Chest) chestLocation2.getState ();
                }

                int maxChestInventorySize = chest.getInventory ().getSize ();
                int filledSlots = 0;

                if (useLargeChest)
                {
                    maxChestInventorySize *= 2;
                }

                saveChestMethod.invoke (null, chest, player);

                // Remove the required chest items from the player's inventory first
                if (requiredChests > 0)
                {
                    ListIterator<ItemStack> inventoryIterator = player.getInventory ().iterator ();
                    while (inventoryIterator.hasNext ())
                    {
                        ItemStack stack = inventoryIterator.next ();
                        if (stack != null)
                        {
                            if (requiredChests > 0 && stack.getType () == Material.CHEST)
                            {
                                // Delete the required chest items from the inventory
                                int initialAmount = stack.getAmount ();
                                stack.setAmount (Math.max (0, stack.getAmount () - requiredChests));
                                requiredChests = Math.max (requiredChests - initialAmount, 0);

                                if (stack.getAmount () == 0)
                                {
                                    // The stack is empty, remove it completely
                                    inventoryIterator.remove ();
                                    continue;
                                }
                            }
                        }
                    }
                }

                ListIterator<net.minecraft.item.ItemStack> droppedItemsIterator = drops
                    .listIterator ();
                while (droppedItemsIterator.hasNext ())
                {
                    net.minecraft.item.ItemStack item = droppedItemsIterator.next ();
                    if (item != null)
                    {
                        ItemStack bukkitStack = CraftItemStack.asCraftMirror (item);
                        if (requiredChests > 0 && bukkitStack.getType () == Material.CHEST)
                        {
                            // Delete the required chest items from the drops list if not enough were in the
                            // inventory of the player
                            int initialAmount = bukkitStack.getAmount ();
                            bukkitStack.setAmount (
                                Math.max (0, bukkitStack.getAmount () - requiredChests));
                            requiredChests = Math.max (requiredChests - initialAmount, 0);

                            if (bukkitStack.getAmount () == 0)
                            {
                                // The stack is empty, remove it completely
                                droppedItemsIterator.remove ();
                                continue;
                            }
                        }

                        if (filledSlots < maxChestInventorySize)
                        {
                            if (filledSlots >= chest.getInventory ().getSize ())
                            {
                                if (!useLargeChest)
                                {
                                    // The single chest is full and no large chest is used - drop the rest
                                    break;
                                }
                                // Fill the second chest
                                chest2.getInventory ()
                                    .setItem (filledSlots % chest.getInventory ().getSize (),
                                        bukkitStack);
                            }
                            else
                            {
                                // Fill the single chest
                                chest.getInventory ().setItem (filledSlots, bukkitStack);
                            }
                            droppedItemsIterator.remove ();
                            ++filledSlots;
                        }
                    }
                }

            }
            catch (IllegalAccessException e)
            {
                logger.errorThrowable ("Couldn't access the deathcraft function \"saveChest\"", e);
            }
            catch (IllegalArgumentException e)
            {
                logger.errorThrowable ("The deathcraft function \"saveChest\" was used wrongly", e);
            }
            catch (InvocationTargetException e)
            {
                logger
                    .errorThrowable ("The deathcraft function \"saveChest\" threw an exception", e);
            }
            finally
            {
                /*
                 * Drop the stacks that couldn't be stored in the chest. Also, the not stored
                 * stacks will be dropped if an unexpected exception occurred, to prevent item
                 * loss.
                 */
                drops.forEach (drop ->
                {
                    event.getPlayer ().func_146097_a (drop, true, false);
                });
            }

        }
        resetData ();
    }

    // Reset all fields which stored player specific data
    private void resetData ()
    {
        deathcraftPluginInstance = null;
        deathcraftClass = null;
        pvpChestEnabledField = null;
        pveChestEnabledField = null;
        deathChestsClass = null;
        canDropChestAtLocationMethod = null;
        goodLocMethod = null;
        goodLocLargeMethod = null;
        saveChestMethod = null;
        utilClass = null;
        infoLogMethod = null;
        player = null;
        requiredChests = 0;
        useLargeChest = false;
        chestLocation = null;
        chestLocation2 = null;
        drops = null;
        canCreateChest = null;
    }

}
