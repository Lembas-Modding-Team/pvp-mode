# pvp-mode
A server-side Minecraft mod that allows management of PvP interaction between players and their hired units in many ways.

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

The mod offers a fully configurable set of management tools for PvP interaction between players and their (LOTRmod) hired units.

First choice to make for server management is to determine whether players will be able to opt out of 'PvP mode', or not. So, whether the server enables 'consented PvP' for players.
If "Enable PvP Toggling" (i.e. consented PvP) is disabled, players will not be able to use the basic command `pvp` to toggle their PvP Mode `ON` or `OFF`.
If enabled, players will be able to use the basic command `pvp` to toggle their PvP Mode `ON` or `OFF`.
To switch into PvP Mode `ON`, a player enters the `pvp` command and their mode will be toggled after a warmup time.
The switch can be canceled by entering the `pvp cancel` command during the warmup period.
When a player switched to PvP Mode `ON`, a cooldown timer starts which ensures a player cannot abuse this mode and immediately switch his PvP Mode to `OFF`.
To switch into PvP Mode `ON`, a player enters the `pvp` command and their mode will be toggled after a warmup time.
The mod has two independently configurable warmup timers for switching PvP Mode. The cooldown timer is also configurable.

Players who are in creative mode or who are flying are automatically prevented from combat no matter their PvP Mode.
There is a cooldown for the command after the actual toggle takes place (not when the player enters the command).
Admins are able to toggle a player's status instantaneously with no warmup time by doing `pvpadmin <playername>`

The mod provides the command `pvplist` to display a list of all players online, their status, and their distance to the
command sender. The status can be one of the following: `ON`, `OFF`, or `WARMUP`. Only players with `ON` are capable of combat.

Servers can enable players with PvP Mode `ON` to get proximity and direction info of other players available for PvP.
Proximity is approximated to the nearest x blocks where x is a number determined in the configuration file (default is 100). Direction info is provided as one of either 8 main wind directions.
If "Allow Per Player Spying Settings" is enabled, this can either be forced for all, or left up to the players to activate using the global setting "Radar".
If left to the players, players can use `pvp spy` to toggle this ability. In that case, they will only get info from and send info to other players who also have their 'little birds' activated.

All players can get actual info on their status with `pvp info`.

For in-game information, use `pvphelp`. This command provides information on all the commands and sub-commands added by the mod. Clicking on the displayed command names automatically inserts them in the chat.

Use `pvpconfig display` to view the values of the server configuration data of PvPMode. This is only for administrators.

The configuration file has many useful options for all the mods features and miscellaneous items.

This mod was developed especially for use on servers running the Lord of the Rings mod by Mevans. Users will note that hired units cannot fight each other unless both their commanding players have PvP enabled. Also, tamed wolves will not attack players or each other unless both masters have PvP enabled.

Besides the abovementioned basic features the mod provides quite a few more advanced features. These can be for general support of managing pvp combat on servers and/or for very specific aspects of the gameplay with the Lord of the Rings mod or other mods.
For more information on those features, please check out the wiki pages of our GitHub portal at https://github.com/VulcanForge/pvp-mode/wiki

If you notice a bug in-game, fire us an issue at our GitHub portal.

Cheers and enjoy your game!
