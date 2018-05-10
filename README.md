# pvp-mode
A server-side Minecraft mod that allows players to enable/disable PvP individually.

### Requirements:

* Minecraft 1.7.10

* Forge 1.7.10-10.13.4.1614 (possibly an earlier version will work)

* Java 8

### Installation:

1. Click the JAR file in the file list above, then click Download.

2. Place the JAR file in the server mods folder.

### Usage:

When a new player logs into the server, PvP will automatically be disabled for them. To enable PvP, a player simply enters the `pvp`
command and their status will be toggled after a warmup time (this warmup can be canceled by entering the `pvpcancel` command).
The `pvp` command also disables PvP for a player who has it enabled.
Players who are in creative mode or who are flying are automatically prevented from combat no matter their PvP status.
There is a cooldown for the command after the actual toggle takes place (not when the player enters the command).
Admins are able to toggle a player's status instantaneously with no warmup time by doing `pvpadmin \<playername>`

The mod adds another command, `pvplist`. `pvplist` displays a list of all players online, their status, and their distance to the
command sender. The status can be one of the following: `ON`, `OFF`, `GM1`, or `FLY`. Only players with `ON` are capable of combat.
Distances are approximated to the nearest x blocks where x is a number determined in the configuration file (default is 64).

For in-game information, use `pvphelp`. This command provides information on all the commands added by the mod, and the values
of the cooldown and warmup time, as well as whether distance radar is enabled.

The configuration file has a few useful options for timers and miscellaneous items.

This mod was developed especially for use on servers running the Lord of the Rings mod by Mevans. Users will note that hired units cannot fight each other unless both their commanding players have PvP enabled. Also, tamed wolves will not attack players or each other unless both masters have PvP enabled.

If you notice a bug in-game, fire me an email at vulcanforgeyt@gmail.com
