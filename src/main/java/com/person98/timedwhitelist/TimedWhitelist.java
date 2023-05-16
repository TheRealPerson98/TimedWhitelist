package com.person98.timedwhitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.person98.timedwhitelist.commands.WhitelistCommand;
import com.person98.timedwhitelist.managers.timedwhitelistExpansion;
import com.person98.timedwhitelist.managers.WhitelistManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class TimedWhitelist extends JavaPlugin {

    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        whitelistManager = new WhitelistManager(this);
        whitelistManager.loadWhitelist();
        WhitelistCommand whitelistCommand = new WhitelistCommand(this, whitelistManager);
        getCommand("timedwhitelist").setExecutor(whitelistCommand);
        getCommand("timedwhitelist").setTabCompleter(whitelistCommand);
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new timedwhitelistExpansion(this, whitelistManager).register();
        }

    }
    public UUID getPlayerUUID(String playerName) {
        // First, check if the player is online
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // If the player is not online, iterate through all known UUIDs
        for (OfflinePlayer cachedPlayer : Bukkit.getOfflinePlayers()) {
            if (cachedPlayer.getName() != null && cachedPlayer.getName().equalsIgnoreCase(playerName)) {
                UUID playerUUID = cachedPlayer.getUniqueId();
                if (playerUUID != null) {
                    return playerUUID;
                }
            }
        }

        // If the player is not found in both online and offline players, use Mojang API
        return getUUIDFromMojangAPI(playerName);
    }

    private UUID getUUIDFromMojangAPI(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
            JsonElement element = new JsonParser().parse(reader);
            String id = element.getAsJsonObject().get("id").getAsString();
            return UUID.fromString(
                    id.substring(0, 8) + "-" +
                            id.substring(8, 12) + "-" +
                            id.substring(12, 16) + "-" +
                            id.substring(16, 20) + "-" +
                            id.substring(20, 32)
            );
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not connect to Mojang API to get UUID for player " + playerName);
            return null;
        }
    }
    @Override
    public void onDisable() {
        whitelistManager.saveWhitelist();
    }
}
