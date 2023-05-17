# TimedWhitelist

TimedWhitelist is a Minecraft plugin that allows server administrators to add players to the whitelist for a specific duration. The plugin also provides features such as sending expiration warnings to players before their whitelist time expires, logging whitelist changes, and sending updates to a Discord channel.

## Setup

Download the TimedWhitelist plugin and place it in your server's plugins directory.
Restart your server to generate the configuration file.
Open the config.yml file in the plugins/TimedWhitelist directory.
Configure the plugin to your liking. You can enable or disable features such as kicking players when their whitelist time expires, sending expiration warnings, logging, and Discord integration.
If you enable Discord integration, you'll need to provide your bot token and the ID of the channel where updates should be sent.
Save the config.yml file and restart your server for the changes to take effect.
Optional Placeholders​
This plugin also provides optional integration with . The following placeholders are available:

%timedwhitelist_time_left%: Shows the time left on the player's whitelist.
%timedwhitelist_whitelisted_players%: Shows the number of players currently whitelisted.
To use these placeholders, you'll need to have PlaceholderAPI installed on your server and replace the placeholders in the config.yml file with the ones above.

Support: Discord.gg/BlockBattles

## Permissions & Commands

/timedwhitelist add <player> <time>: Adds a player to the whitelist for a specific duration. Requires timedwhitelist.add permission.
/timedwhitelist addtime <player> <time>: Adds time to a player's whitelist duration. Requires timedwhitelist.addtime permission.
/timedwhitelist removetime <player> <time>: Removes time from a player's whitelist duration. Requires timedwhitelist.removetime permission.
/timedwhitelist remove <player>: Removes a player from the whitelist. Requires timedwhitelist.remove permission.
/timedwhitelist list: Lists players on the whitelist. Requires timedwhitelist.list permission.
/timedwhitelist list all: Lists all players on the whitelist. Requires timedwhitelist.list.all permission.
/timedwhitelist toggle: Toggles the whitelist on and off. Requires timedwhitelist.toggle permission.

timedwhitelist.add: Permission to add players to the whitelist.
timedwhitelist.addtime: Permission to add time to a player's whitelist.
timedwhitelist.removetime: Permission to remove time from a player's whitelist.
timedwhitelist.remove: Permission to remove players from the whitelist.
timedwhitelist.list: Permission to list players on the whitelist.
timedwhitelist.list.all: Permission to list all players on the whitelist.
timedwhitelist.toggle: Permission to toggle whitelist on and off.

## Configuration

The config.yml file for the TimedWhitelist plugin allows you to customize various aspects of the plugin's behavior. Here's a breakdown of the available options:

kickOnWhitelistEnd: If set to true, players will be kicked from the server when their whitelist time expires. Default is true.
sendExpirationWarnings: If set to true, players will receive warnings before their whitelist time expires. Default is true.
enableWhitelist: If set to true, the whitelist will be enabled. Default is true.
enableLogging: If set to true, whitelist changes will be logged. Default is true.
timeleftplaceholder: Placeholder for the time left on a player's whitelist. Default is "time_left".
whitelistedplayersplaceholder: Placeholder for the number of players currently whitelisted. Default is "whitelisted_players".
discordIntegrationEnabled: If set to true, updates will be sent to a Discord channel. Default is false.
discordBotToken: Your Discord bot token. Required if discordIntegrationEnabled is true.
discordChannelId: The ID of the Discord channel where updates should be sent. Required if discordIntegrationEnabled is true.
messages: A collection of messages that the plugin can send to players. Each message can include placeholders for player names and times.