# pvp-mode
A server-side Minecraft mod that allows players to enable/disable PvP individually.

### Requirements:

* Minecraft 1.7.10

* Forge 1.7.10-10.13.4.1614 (possibly an earlier version will work)

* Java 8

### Installation:

1. Click the JAR file in the file list above, then click Download.

2. Place the JAR file in the server mods folder.

### Compatibility:

The mod is highly compatible with, and in many cases offers dedicated feature for, the LOTRmod and the SiegeMode mod. Compatibility is also aranged for some features of SuffixForge, a serverside utility and roleplay mod. See the wiki page for more info on this.

### Usage:

When a new player logs into the server, PvP will automatically be disabled for them. To enable PvP, a player simply enters the `pvp`
command and their status will be toggled after a warmup time (this warmup can be canceled by entering the `pvp cancel` command).
The `pvp` command also disables PvP for a player who has it enabled.

Players who are in creative mode or who are flying are automatically prevented from combat no matter their PvP status.
There is a cooldown for the command after the actual toggle takes place (not when the player enters the command).
Admins are able to toggle a player's status instantaneously with no warmup time by doing `pvpadmin <playername>`

The mod adds another command, `pvplist`. `pvplist` displays a list of all players online, their status, and their distance to the
command sender. The status can be one of the following: `ON`, `OFF`, or `WARMUP`. Only players with `ON` are capable of combat.

Servers can enable players with PvP Mode `ON` to get proximity and direction info of other players available for PvP. If this is anebled, this can either be forced for all, or left up to the players to activate. If left to the players, players can use `pvp spy` to toggle this ability. They will only get info from and send info to others who also have their 'little birds' activated.

Proximity is approximated to the nearest x blocks where x is a number determined in the configuration file (default is 100). Direction info is provided as one of either 8 main wind directions.

All players can get actual info on their status with `pvp info`.

For in-game information, use `pvphelp`. This command provides information on all the commands and sub-commands added by the mod. Clicking on the displayed command names automatically inserts them in the chat.

Use `pvpconfig display` to view the values of the server configuration data of PvPMode. This is only for administrators.

The configuration file has a few useful options for timers and miscellaneous items.

This mod was developed especially for use on servers running the Lord of the Rings mod by Mevans. Users will note that hired units cannot fight each other unless both their commanding players have PvP enabled. Also, tamed wolves will not attack players or each other unless both masters have PvP enabled.

Besides the abovementioned basic features the mod provides quite a few more advanced features. These can be for general support of managing pvp combat on servers and/or for very specific aspects of the gameplay with the Lord of the Rings mod or other mods.
For more information on those features, please check out the wiki pages of our GitHub portal at https://github.com/VulcanForge/pvp-mode/wiki

If you notice a bug in-game, fire us an issue at our GitHub portal, or send an email to vulcanforgeyt@gmail.com.

Cheers and enjoy your game!
