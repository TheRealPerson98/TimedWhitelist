package com.person98.extendedwhitelist.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import com.person98.extendedwhitelist.ExtendedWhitelist;
import com.person98.extendedwhitelist.managers.WhitelistManager;

public class ExtendedWhitelistExpansion extends PlaceholderExpansion {

    private ExtendedWhitelist plugin;
    private WhitelistManager whitelistManager;

    public ExtendedWhitelistExpansion(ExtendedWhitelist plugin, WhitelistManager whitelistManager) {
        this.plugin = plugin;
        this.whitelistManager = whitelistManager;

    }

    @Override
    public String getIdentifier() {
        return "extendedwhitelist";
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

        // %extendedwhitelist_time_left%
        if ("time_left".equals(identifier)) {
            long timeLeft = whitelistManager.getTimeLeft(player.getUniqueId());
            return formatTime(timeLeft);
        }
        return null;
    }

    private String formatTime(long timeInSeconds) {
        long hours = timeInSeconds / 3600;
        long minutes = (timeInSeconds % 3600) / 60;
        long seconds = timeInSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}

