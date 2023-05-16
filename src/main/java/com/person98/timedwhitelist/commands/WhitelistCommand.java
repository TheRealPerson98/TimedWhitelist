package com.person98.timedwhitelist.commands;

import com.person98.timedwhitelist.TimedWhitelist;
import com.person98.timedwhitelist.managers.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WhitelistCommand implements CommandExecutor, TabCompleter {

    private TimedWhitelist plugin;
    private WhitelistManager whitelistManager;
    private FileConfiguration config;
    boolean shouldKick;
    private boolean isWhitelistEnabled;

    public WhitelistCommand(TimedWhitelist plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
        this.config = plugin.getConfig();
        this.shouldKick = config.getBoolean("kickOnWhitelistRemoval");
        this.isWhitelistEnabled = config.getBoolean("enableWhitelist");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isWhitelistEnabled) {
            sender.sendMessage(ChatColor.RED + "The whitelist is currently disabled.");
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /timedwhitelist <add|addtime|removetime|list> [player] <time>");
            return false;
        }

        String action = args[0].toLowerCase();
        String playerName;

        switch (action) {
            case "add":
            case "addtime":
            case "removetime":
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /timedwhitelist " + action + " <player> <time>");
                    return false;
                }

                playerName = args[1];
                String durationStr = args[2];

                long duration;
                try {
                    duration = parseDuration(durationStr);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid duration. Please enter a valid duration (e.g., 1s, 1m, 1h, 1d, 1w, 1mth, 1y).");
                    return false;
                }

                UUID playerUUID = plugin.getPlayerUUID(playerName);
                if (playerUUID == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return false;
                }

                try {
                    switch (action) {
                        case "add":
                            if (!sender.hasPermission("timedwhitelist.add")) {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to add players to the whitelist.");
                                return false;
                            }
                            whitelistManager.addPlayerToWhitelist(playerUUID, duration);
                            Player player = Bukkit.getPlayer(playerUUID);
                            if (player != null) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.yourWhitelistTimeExtended").replace("%time%", durationStr)));
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.playerWhitelisted").replace("%player%", playerName).replace("%time%", durationStr)));
                            break;

                        case "addtime":
                            if (!sender.hasPermission("timedwhitelist.addtime")) {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to add players to the whitelist.");
                                return false;
                            }
                            whitelistManager.addTimeToWhitelist(playerUUID, duration);
                            Player player2 = Bukkit.getPlayer(playerUUID);
                            if (player2 != null) {
                                player2.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.yourWhitelistTimeExtended").replace("%time%", durationStr)));
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.timeAddedToWhitelist").replace("%player%", playerName).replace("%time%", durationStr)));
                            break;

                        case "removetime":
                            if (!sender.hasPermission("timedwhitelist.removetime")) {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to add players to the whitelist.");
                                return false;
                            }
                            whitelistManager.removeTimeFromWhitelist(playerUUID, duration);
                            Player player1 = Bukkit.getPlayer(playerUUID);
                            if (player1 != null) {
                                player1.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.yourWhitelistTimeRemoved").replace("%time%", durationStr)));
                            }
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.timeRemovedFromWhitelist").replace("%player%", playerName).replace("%time%", durationStr)));
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(e.getMessage());
                    return false;
                }

                break;
            case "on":
                if (!sender.hasPermission("timedwhitelist.toggle")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to toggle the whitelist.");
                    return false;
                }
                isWhitelistEnabled = true;
                Bukkit.setWhitelist(true);
                config.set("enableWhitelist", true);
                Bukkit.setWhitelist(true);

                sender.sendMessage(ChatColor.GREEN + "Whitelist has been enabled.");
                return true;

            case "off":
                if (!sender.hasPermission("timedwhitelist.toggle")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to toggle the whitelist.");
                    return false;
                }
                isWhitelistEnabled = false;
                Bukkit.setWhitelist(false);
                config.set("enableWhitelist", false);
                Bukkit.setWhitelist(true);

                sender.sendMessage(ChatColor.GREEN + "Whitelist has been disabled.");
                return true;
            case "remove":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ExtendedWhitelist remove <player>");
                    return false;
                }
                if (!sender.hasPermission("timedwhitelist.remove")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to remove players from the whitelist.");
                    return false;
                }
                playerName = args[1];
                UUID playerUUIDToRemove = plugin.getPlayerUUID(playerName);
                if (playerUUIDToRemove == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return false;
                }

                whitelistManager.removePlayerFromWhitelist(playerUUIDToRemove);

                if (shouldKick) {
                    Player playerToRemove = Bukkit.getPlayer(playerUUIDToRemove);
                    if (playerToRemove != null) {
                        playerToRemove.kickPlayer(ChatColor.translateAlternateColorCodes('&', config.getString("messages.whitelistExpired")));
                    }
                }

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.playerRemovedFromWhitelist").replace("%player%", playerName)));
                break;
            case "list":
                if (args.length > 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /timedwhitelist list [player] [page]");
                    return false;
                }
                if (!sender.hasPermission("timedwhitelist.list")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to list players.");
                    return false;
                }
                try {
                    if (args.length == 1) {
                        // If no player is specified, use the sender's name
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                            return false;
                        }

                        Player player = (Player) sender;
                        long timeLeft = whitelistManager.getTimeLeft(player.getUniqueId());
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.playerTimeLeft")).replace("%time%", formatTime(timeLeft)));
                    } else if (args[1].equalsIgnoreCase("all")) {
                        if (!sender.hasPermission("timedwhitelist.list.all")) {
                            sender.sendMessage(ChatColor.RED + "You do not have permission to list all players.");
                            return false;
                        }
                        int page = 1;
                        if (args.length == 3) {
                            try {
                                page = Integer.parseInt(args[2]);
                            } catch (NumberFormatException e) {
                                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                                return false;
                            }
                        }
                        List<OfflinePlayer> whitelistedPlayers = whitelistManager.getAllWhitelistedPlayers();
                        int playersPerPage = 10;
                        int totalPages = (int) Math.ceil((double) whitelistedPlayers.size() / playersPerPage);
                        if (page > totalPages || page < 1) {
                            sender.sendMessage(ChatColor.RED + "Invalid page number.");
                            return false;
                        }
                        int startIndex = (page - 1) * playersPerPage;
                        int endIndex = Math.min(whitelistedPlayers.size(), startIndex + playersPerPage);

                        sender.sendMessage(ChatColor.GREEN + "Whitelisted players (Page " + page + " of " + totalPages + "):");
                        for (int i = startIndex; i < endIndex; i++) {
                            OfflinePlayer player = whitelistedPlayers.get(i);
                            long timeLeft = whitelistManager.getTimeLeft(player.getUniqueId());
                            sender.sendMessage(ChatColor.GREEN + player.getName() + " - " + formatTime(timeLeft) + " left on the whitelist.");
                        }
                    } else {
                        playerName = args[1];
                        UUID playerUUID1 = plugin.getPlayerUUID(playerName);
                        if (playerUUID1 == null) {
                            sender.sendMessage(ChatColor.RED + "Player not found.");
                            return false;
                        }

                        long timeLeft = whitelistManager.getTimeLeft(playerUUID1);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.playerWhitelistStatus")).replace("%player%", playerName).replace("%time%", formatTime(timeLeft)));
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(e.getMessage());
                    return false;
                }

                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid action. Valid actions are add, addtime, removetime, list.");
                return false;
        }

        return true;
    }

    private String formatTime(long seconds) {
        long years = seconds / (60 * 60 * 24 * 365);
        seconds %= 60 * 60 * 24 * 365;
        long months = seconds / (60 * 60 * 24 * 30);
        seconds %= 60 * 60 * 24 * 30;
        long weeks = seconds / (60 * 60 * 24 * 7);
        seconds %= 60 * 60 * 24 * 7;
        long days = seconds / (60 * 60 * 24);
        seconds %= 60 * 60 * 24;
        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append(years == 1 ? " year " : " years ");
        if (months > 0) sb.append(months).append(months == 1 ? " month " : " months ");
        if (weeks > 0) sb.append(weeks).append(weeks == 1 ? " week " : " weeks ");
        if (days > 0) sb.append(days).append(days == 1 ? " day " : " days ");
        if (hours > 0) sb.append(hours).append(hours == 1 ? " hour " : " hours ");
        if (minutes > 0) sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        if (seconds > 0) sb.append(seconds).append(seconds == 1 ? " second " : " seconds ");
        return sb.toString().trim();
    }


    private long parseDuration(String str) throws IllegalArgumentException {
        long value = Long.parseLong(str.substring(0, str.length() - 1));
        char unit = str.charAt(str.length() - 1);

        switch (unit) {
            case 's':
                return value;
            case 'm':
                return value * 60;
            case 'h':
                return value * 60 * 60;
            case 'd':
                return value * 60 * 60 * 24;
            case 'w':
                return value * 60 * 60 * 24 * 7;
            case 'y':
                return value * 60 * 60 * 24 * 365; // Approximate
            default:
                throw new IllegalArgumentException(ChatColor.RED + "Invalid time unit. Valid units are s, m, h, d, w, y.");
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("add", "addtime", "removetime", "remove", "list", "on", "off");
            List<String> completions = new ArrayList<>();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0])) {
                    completions.add(subCommand);
                }
            }
            return completions;
        }
        return null;
    }
}


