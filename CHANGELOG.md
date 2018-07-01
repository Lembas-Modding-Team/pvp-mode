# Changelog of PvPMode for Minecraft 1.7.10:

## 1.2.0-BETA
### General changes:
* Fixed that players drop too less items with partial inventory loss under rare circumstances
* Added a configurable "main inventory loss" to the partial inventory loss
* Transferring items via shift-clicking in the player's inventory during PvP is now disableable
* If an inventory contains less stacks than have to be dropped, the game can optionally scan other inventories
* Players can now optionally decide with `pvp spy [on|off]` whether they want to provide/receive proximity informations
* Increased the default PvP timer value to 45 seconds
* Increased the maximum possible value of the PvP timer to 300 seconds
* Added an on-off warmup timer which specifies the toggle time from PvP ON to PvP OFF
* The default value of the off-on warmup timer is now 30 seconds

### LOTR compatibility:
* Players now optionally drop their skulls when killed with a weapon with the headhunter modifier even if keepInventory is on

### SuffixForge compatibility:
* Added a configuration option to prevent soulbound items to be dropped with the partial inventory loss

## 1.1.4-BETA
* Fixed that players switching from PvP on to off couldn't do PvP during the warmup phase
* Fixed that the warmup timer was displayed in an incorrect way sometimes in `pvplist`

## 1.1.3-BETA
* Fixed that the PvP timer wasn't resetted for creative/flying players
* Fixed that a chat message wasn't displayed correctly with small chat widths

## 1.1.2-BETA
* Fixed that the entries in `pvplist` with PvP on were sorted by descending proximity

## 1.1.1-BETA
* Fixed that the conditional overrides were applied delayed to some players
* Fixed that using `pvp` while the warmup timer is running restarts it
* Fixed that creative/flying players could use `pvp` (and `pvpadmin` could used on them)
* Fixed that the warmup timer of creative/flying players wasn't canceled

## 1.1.0-BETA
### General changes:
* The changelog will now be integrated into the JAR of PvPMode
* Restructured the internal handling of compatibility-related code (added compatibility modules)
* Added a warning if the attacking player has PvP disabled
* Clicking on `pvpadmin` in `pvphelp` now appends a space to the suggested chat input
* The calling player of `pvplist` now will always be displayed on the top of the list
* Added a footer to the `pvphelp` command list
* Added a header and a footer to the `pvplist` player list
* Fixed that the chat text formattings were screwed up with small chat widths
* Players in the `pvplist` list with PvP enabled are now sorted by their proximity (if radar is enabled)
* Fixed that the `pvphelp` command list contains unnecessary spaces
* Added a configurable partial armour and hotbar inventory loss (applies only when keepInventory is true)
* Added general support for conditional PvP mode overrides (forcing PvP mode to OFF or ON)
* Added missing comments to some configuration properties
* The PvP mode of players involved in a PvP event cannot be changed anymore
* Added a command blacklist for players which are in PvP: While in PvP, they cannot use the blacklisted commands
* The commands added by PvP mode now behave uniformly regarding invalid usage
* Fixed that players which aren't able to do PvP could see the proximity informations in `pvplist`

### LOTR compatibility:
* Improved the performance if the LOTR Mod is present
* Added a configurable override condition for the LOTR Mod (players in enemy biomes will have PvP mode ON)
* A file containing the LOTR biome ids now will be generated on startup (if the LOTR mod is present)
* Fixed that hired units from the LOTR Mod start attacking players/units with PvP mode OFF (without causing damage)
* Players which are in PvP cannot use the LOTR Mod fast travel system

## 1.0.0-BETA
* Added combat logging (two handlers: csv (default) and simple)
* Added a command `pvpconfig` for admins which displays the configuration data
* Java 8 is now required
* The default distance rounding factor for the player radar is now 100 instead of 64
* Fixed that typing invalid player names in `pvpadmin` outputs an unmeaningful error message
* Improved `pvphelp`. Also it won't display configuration data anymore.
* Removed `pvpcancel`. Canceling now will be done via `pvp cancel`
* Improved the performance and restructured things internally

## 1.8.0-ALPHA
* Added a new command `pvphelp` which displays usage of all commands and also values of configuration options.
* Player radar now rounds up rather than down.

## 1.7.0-ALPHA
* Minor tweaks as suggested by MilkMC and AlteOgre.
* The admin PvP command is now a separate command, `pvpadmin <playername>`
* Added a new command `pvpcancel` which cancels a player's warmup time.

## 1.6.0-ALPHA
* The cooldown and warmup are now configurable in a configuration file.
* Player radar behaviour is now configurable as well.

## 1.5.0-ALPHA
* The `pvplist` command will now display player distances to the nearest four chunks for players who have PvP enabled. There is now a cooldown as well as a warmup on the `pvp` command; the warmup is now 15 seconds rather than five.

## 1.4.1-ALPHA
* Players in creative mode or who have fly ability have PvP disabled automatically.

## 1.4.0-ALPHA
* Added a 5-second cooldown on the `pvp` command.
* Removed on/off functionality; the command is now simply a toggle.
* Added some messages that will send debug info to the log. This is so that the LOTR unit problem can be fixed.

## 1.3.0-ALPHA
* Added a `pvplist` command that enables players to see who is PvP enabled or disabled.
* Temporarily removed the config file option preparatory to LOTR mod testing.

## 1.2.0-ALPHA
* Added a patch that disables LOTR units from attacking other players in PvP-off situations.
* Added a patch that disables tamed wolves from attacking another player.

## 1.1.0-ALPHA
* Added a config option that disables PvE along with PvP if the command is entered.
* Fixed a bug with permission levels.

## 1.0.0-ALPHA
* Added `pvp <on:off> [playername]` command.
* Players cannot hit or shoot each other if either has PvP disabled.
* Admins can enable/disable PvP for other players, but regular players can only do it on themselves.
