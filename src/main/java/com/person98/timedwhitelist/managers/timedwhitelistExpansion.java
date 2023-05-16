package com.person98.timedwhitelist.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.person98.timedwhitelist.TimedWhitelist;

public class timedwhitelistExpansion extends PlaceholderExpansion {

    private TimedWhitelist plugin;
    private WhitelistManager whitelistManager;
    private FileConfiguration config;

    public timedwhitelistExpansion(TimedWhitelist plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;
        this.config = plugin.getConfig();

    }

    @Override
    public String getIdentifier() {
        return "timedwhitelist";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if(player == null){
            return "";
        }

        if (config.getString("timeleftplaceholder").equals(identifier)) {
            long timeLeft = whitelistManager.getTimeLeft(player.getUniqueId());
            if (timeLeft == -1) {
                return ChatColor.translateAlternateColorCodes('&', config.getString("messages.playerNotOnWhitelist"));
            }
            return formatTime(timeLeft);
        }

        if (config.getString("whitelistedplayersplaceholder").equals(identifier)) {
            int whitelistedPlayers = whitelistManager.getAllWhitelistedPlayers().size();
            if (whitelistedPlayers == 0) {
                return ChatColor.translateAlternateColorCodes('&', config.getString("messages.noPlayersOnWhitelist"));
            }
            return String.valueOf(whitelistedPlayers);
        }

        return null;
    }


    private String formatTime(long timeInSeconds) {
        if (timeInSeconds == 0) {
            return ChatColor.translateAlternateColorCodes('&', config.getString("messages.noTimeLeft"));
        }

        long years = timeInSeconds / (60 * 60 * 24 * 365);
        timeInSeconds = timeInSeconds % (60 * 60 * 24 * 365);

        long months = timeInSeconds / (60 * 60 * 24 * 30);
        timeInSeconds = timeInSeconds % (60 * 60 * 24 * 30);

        long days = timeInSeconds / (60 * 60 * 24);
        timeInSeconds = timeInSeconds % (60 * 60 * 24);

        long hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds % 3600;

        long minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds % 60;

        long seconds = timeInSeconds;

        StringBuilder sb = new StringBuilder();
        if (years > 0) {
            sb.append(years).append(":");
        }
        if (months > 0) {
            sb.append(months).append(":");
        }
        if (days > 0) {
            sb.append(days).append(":");
        }
        if (hours > 0) {
            sb.append(hours).append(":");
        }
        if (minutes > 0) {
            sb.append(minutes).append(":");
        }
        if (seconds > 0) {
            sb.append(seconds).append("");
        }
        return sb.toString().trim();
    }

}

