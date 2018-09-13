# Update instructions for the current release: 2.0.0-BETA

## Permission node changes:
The left parts are the old node names, the right parts are the new ones:
* `pvpmode.command.pvpcommand` --> `pvpmode.internal.server.command.pvpcommand`
* `pvpmode.command.pvpcommandadmin` --> `pvpmode.internal.server.command.pvpcommandadmin`
* `pvpmode.command.pvpcommandlist` --> `pvpmode.internal.server.command.pvpcommandlist`
* `pvpmode.command.pvpcommandhelp` --> `pvpmode.internal.server.command.pvpcommandhelp`
* `pvpmode.command.pvpcommandconfig` --> `pvpmode.internal.server.command.pvpcommandconfig`

## Configuration changes:
The configuration system changed a lot - the old configuration files are no longer used and the structure of the new ones is very different from the old ones. The PvP Mode Mod has to be reconfigured completely.

### General changes:
* All configuration files are now located at the directory `config/pvp-mode`
* Subdirectories contain configuration files for the compatibility modules (example: `config/pvp-mode/lotr`)
* The configuration file `pvp-mode.cfg` no longer contains configuration entries for the compatibility modules

### Renamed properties:
#### General configuration:
* `Active Pvp Logging Handlers` --> `Active Combat Logging Handlers`
* `Warmup (seconds)` --> `Warmup off-on (seconds)`
* `Radar` --> `Intelligence Enabled`
* `CSV separator` --> `CSV Separator`
* `Enable Partial Inventory Loss` --> `PvP Partial Inventory Loss Enabled`
* `Enable Partial Inventory Loss For PvE` --> `PvE Partial Inventory Loss Enabled`
* `Armour Item Loss` --> `PvP Armour Item Loss`
* `Hotbar Item Loss` --> `PvP Hotbar Item Loss`
* `Main Item Loss` --> `PvP Main Item Loss`
* `PvP Mode Override Check Interval (Seconds)` --> `Override Check Interval (seconds)`
* `PvP Timer (Seconds)` --> `PvP Timer (seconds)`
* `Allow Per Player Spying Settings` --> `Per Player Spying Settings Allowed`
* `Enable PvP Toggling` --> `PvP Toggling Enabled`
* `Disable Fast Item Transfer` --> `Fast Item Transfer Disabled`
* `Extend Armour Inventory Search` --> `Armour Inventory Search Extended`
* `Extend Hotbar Inventory Search` --> `Hotbar Inventory Search Extended`
* `Extend Main Inventory Search` --> `Main Inventory Search Extended`
* `Allow Indirect PvP` --> `Indirect PvP Allowed`
* `Command Blacklist` --> `Blocked Commands`
* `Prefix Global Chat Messages` --> `Global Chat Messages Prefixed`
* `Announce PvP Enabled Globally` --> `PvP Enabled Announced Globally`
* `Announce PvP Disabled Globally` --> `PvP Disabled Announced Globally`
* `Force Default PvP Mode` --> `Default PvP Mode Forced`
* `Show Proximity Direction` --> `Proximity Direction Shown`

#### LOTR compatibility configuration:
* `Enable enemy biome override condition` --> `Enemy Biome Overrides Enabled`
* `Enable safe biome override condition` --> `Save Biome Overrides Enabled`
* `Block fast traveling in PvP` --> `Fast Traveling While PvP Blocked`

#### Siege Mode compatibility configuration:
* `Disable PvP Logging During Sieges` --> `PvP Logging During Sieges Disabled`

#### Suffix Forge compatibility configuration:
* `Drop Soulbound Items` --> `Soulbound Items Dropped`