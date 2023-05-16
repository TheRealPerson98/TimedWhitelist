package com.person98.extendedwhitelist.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.person98.extendedwhitelist.ExtendedWhitelist;
import com.person98.extendedwhitelist.managers.WhitelistManager;

public class ExtendedWhitelistExpansion extends PlaceholderExpansion {

    private ExtendedWhitelist plugin;
    private WhitelistManager whitelistManager;
    private FileConfiguration config;

    public ExtendedWhitelistExpansion(ExtendedWhitelist plugin, WhitelistManager whitelistManager) {
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
        long hours = timeInSeconds / 3600;
        long minutes = (timeInSeconds % 3600) / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}

