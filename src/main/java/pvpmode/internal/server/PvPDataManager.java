package pvpmode.internal.server;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import pvpmode.api.common.utils.PvPCommonUtils;
import pvpmode.api.server.PvPData;
import pvpmode.api.server.utils.PvPServerUtils;

public class PvPDataManager
{

    private final ServerProxy server;
    private Path playerDataRootDir;

    private final Map<UUID, PvPDataImpl> playerData = new HashMap<> ();

    public PvPDataManager (ServerProxy server)
    {
        this.server = server;
    }

    public void onServerStarting () throws IOException
    {
        playerDataRootDir = server.getWorldDataFolder ().resolve ("playerdata");

        Files.createDirectories (playerDataRootDir);
    }

    public void onLogin (EntityPlayer player)
    {
        loadPlayerData (player.getUniqueID (), compatibilityPatch (player));
        setCacheData (player);
    }

    public void onLogout (EntityPlayer player)
    {
        setCacheData (player);
    }

    private void setCacheData (EntityPlayer player)
    {
        PvPDataImpl data = (PvPDataImpl) getPlayerData (player);

        data.setCanFlyCache (PvPServerUtils.canFly (player));
        data.setCreativeCache (PvPCommonUtils.isCreativeMode (player));
    }

    public PvPData getPlayerData (EntityPlayer player)
    {
        return getPlayerData (player.getUniqueID ());
    }

    public PvPData getPlayerData (UUID playerUUID)
    {
        if (!playerData.containsKey (playerUUID))
        {
            playerData.put (playerUUID, this.loadPlayerData (playerUUID, null));
        }
        return playerData.get (playerUUID);
    }

    @SuppressWarnings("unchecked")
    public void saveAndClearData ()
    {
        playerData.forEach (this::savePlayerData);
        Collection<UUID> presentPlayers = ((Collection<EntityPlayerMP>) server.getServerConfigurationManager ().playerEntityList)
                .stream ().map (Entity::getUniqueID).collect (Collectors.toSet())
        playerData.keySet ().removeIf (uuid -> !presentPlayers.contains (uuid));
    }

    private PvPDataImpl loadPlayerData (UUID playerUUID, NBTTagCompound compatibilityTag)
    {
        Path playerDataFile = getPlayerDataFile (playerUUID);
        NBTTagCompound pvpDataTag = compatibilityTag != null ? compatibilityTag : new NBTTagCompound ();
        boolean shouldBeSaved = false;

        if (!Files.exists (playerDataFile) || compatibilityTag != null)
        {
            shouldBeSaved = true;
        }
        else
        {
            try
            {
                pvpDataTag = CompressedStreamTools.read (playerDataFile.toFile ());
            }
            catch (IOException e)
            {
                server.getLogger ().errorThrowable ("Couldn't load the playerdata for the player \"%s\"", e,
                    playerUUID);
            }
        }

        PvPDataImpl data = new PvPDataImpl (pvpDataTag, playerUUID);

        if (shouldBeSaved)
        {
            savePlayerData (playerUUID, data);
        }

        return data;
    }

    private static final String PVP_DATA_NBT_KEY = "PvPData";

    private NBTTagCompound compatibilityPatch (EntityPlayer player)
    {
        NBTTagCompound data = player.getEntityData ();
        NBTTagCompound persistent;

        if (data.hasKey (EntityPlayer.PERSISTED_NBT_TAG))
        {
            persistent = data.getCompoundTag (EntityPlayer.PERSISTED_NBT_TAG);

            if (persistent.hasKey (PVP_DATA_NBT_KEY))
            {
                NBTTagCompound ret = persistent.getCompoundTag (PVP_DATA_NBT_KEY);
                persistent.removeTag (PVP_DATA_NBT_KEY);
                server.getLogger ().info ("Converted the old PvP Mode Mod player data of \"%s\" to the new format",
                    player.getDisplayName ());
                return ret;
            }
        }

        return null;

    }

    private void savePlayerData (UUID playerUUID, PvPDataImpl playerData)
    {
        Path playerDataFile = getPlayerDataFile (playerUUID);

        boolean newFileCreated = false;

        if (!Files.exists (playerDataFile))
        {
            try
            {
                Files.createFile (playerDataFile);
                newFileCreated = true;
            }
            catch (IOException e)
            {
                server.getLogger ().errorThrowable ("Couldn't create the player data file \"%s\"", e,
                    playerDataFile.toString ());
            }
        }

        try
        {
            // Only save if the player data did change or were created for the first time
            if (newFileCreated || playerData.hasChanged ())
            {
                CompressedStreamTools.write (playerData.getStorageTag (), playerDataFile.toFile ());
            }
        }
        catch (IOException e)
        {
            server.getLogger ().errorThrowable ("Couldn't save the player data file \"%s\"", e,
                playerDataFile.toString ());
        }
    }

    private Path getPlayerDataFile (UUID playerUUID)
    {
        return playerDataRootDir.resolve (playerUUID.toString () + ".dat");
    }

}
