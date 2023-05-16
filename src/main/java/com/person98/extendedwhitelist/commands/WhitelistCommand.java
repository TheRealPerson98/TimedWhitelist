package com.person98.extendedwhitelist.commands;

import com.person98.extendedwhitelist.ExtendedWhitelist;
import com.person98.extendedwhitelist.managers.WhitelistManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhitelistCommand implements CommandExecutor {

    private ExtendedWhitelist plugin;
    private WhitelistManager whitelistManager;

    public WhitelistCommand(ExtendedWhitelist plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /ExtendedWhitelist <add|addtime|removetime|list> [player] <time>");
            return false;
        }

        String action = args[0].toLowerCase();
        String playerName;

        switch (action) {
            case "add":
            case "addtime":
            case "removetime":
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ExtendedWhitelist " + action + " <player> <time>");
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

                try {
                    switch (action) {
                        case "add":
                            whitelistManager.addPlayerToWhitelist(playerName, duration);
                            sender.sendMessage(ChatColor.GREEN + "Player " + playerName + " has been whitelisted for " + durationStr + ".");
                            break;
                        case "addtime":
                            whitelistManager.addTimeToWhitelist(playerName, duration);
                            sender.sendMessage(ChatColor.GREEN + "Added " + durationStr + " to " + playerName + "'s whitelist duration.");
                            break;
                        case "removetime":
                            whitelistManager.removeTimeFromWhitelist(playerName, duration);
                            sender.sendMessage(ChatColor.RED + "Removed " + durationStr + " from " + playerName + "'s whitelist duration.");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(e.getMessage());
                    return false;
                }

                break;
            case "list":
                if (args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ExtendedWhitelist list [player]");
                    return false;
                }

                try {
                    if (args.length == 1) {
                        // If no player is specified, use the sender's name
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                            return false;
                        }

                        long timeLeft = whitelistManager.getTimeLeft(sender.getName());
                        sender.sendMessage(ChatColor.GREEN + "You have " + formatTime(timeLeft) + " left on the whitelist.");
                    } else {
                        playerName = args[1];
                        long timeLeft = whitelistManager.getTimeLeft(playerName);
                        sender.sendMessage(ChatColor.GREEN + playerName + " has " + formatTime(timeLeft) + " left on the whitelist.");
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
            case 's': return value;
            case 'm': return value * 60;
            case 'h': return value * 60 * 60;
            case 'd': return value * 60 * 60 * 24;
            case 'w': return value * 60 * 60 * 24 * 7;
            case 'y': return value * 60 * 60 * 24 * 365; // Approximate
            default: throw new IllegalArgumentException("Invalid time unit. Valid units are s, m, h, d, w, y.");
        }
    }
}

