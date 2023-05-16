package com.person98.extendedwhitelist.commands;

import com.person98.extendedwhitelist.ExtendedWhitelist;
import com.person98.extendedwhitelist.managers.WhitelistManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WhitelistCommand implements CommandExecutor {

    private ExtendedWhitelist plugin;
    private WhitelistManager whitelistManager;

    public WhitelistCommand(ExtendedWhitelist plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ExtendedWhitelist <add|addtime|removetime> <player> <time>");
            return false;
        }

        String action = args[0].toLowerCase();
        String playerName = args[1];
        String durationStr = args[2];

        long duration;
        try {
            duration = parseDuration(durationStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid duration. Please enter a valid duration (e.g., 1s, 1m, 1h, 1d, 1w, 1y).");
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
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid action. Valid actions are add, addtime, removetime.");
                    return false;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(e.getMessage());
            return false;
        }

        return true;
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

