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

The general changes are:
* All configuration files are now located at the directory `config/pvp-mode`
* Subdirectories contain configuration files for the compatibility modules (example: `config/pvp-mode/lotr`)
* The configuration file `pvp-mode.cfg` no longer contains configuration entries for the compatibility modules