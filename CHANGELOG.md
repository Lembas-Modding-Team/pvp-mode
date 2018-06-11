# Changelog of PvPMode for Minecraft 1.7.10:

## 1.1.0-BETA
* The changelog will now be integrated into the JAR of PvPMode
* Restructured the internal handling of compatibility-related code (added compatibility modules)
* Improved the performance if the LOTR Mod is present
* The calling player of `pvplist` now will always be displayed on the top of the list

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
