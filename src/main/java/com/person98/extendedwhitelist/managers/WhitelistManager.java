package com.person98.extendedwhitelist.managers;

import com.person98.extendedwhitelist.ExtendedWhitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WhitelistManager {
    private final ExtendedWhitelist plugin;
    private final File whitelistFile;
    private final FileConfiguration whitelistConfig;
    private final Map<String, BukkitRunnable> tasks = new HashMap<>();

    public WhitelistManager(ExtendedWhitelist plugin) {
        this.plugin = plugin;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        this.whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
    }

    public void addPlayerToWhitelist(String playerName, long durationInSeconds) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        player.setWhitelisted(true);
        whitelistConfig.set(playerName, System.currentTimeMillis() / 1000 + durationInSeconds);
        whitelistConfig.set(playerName + ".duration", durationInSeconds);
        whitelistConfig.set(playerName + ".addedAt", System.currentTimeMillis() / 1000);

        saveWhitelist();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                removePlayerFromWhitelist(playerName);
                if (player.isOnline()) {
                    player.getPlayer().sendMessage("Your time on the whitelist has expired!");
                }
            }
        };
        task.runTaskLater(plugin, durationInSeconds * 20);
        tasks.put(playerName, task);
    }
    public void addTimeToWhitelist(String playerName, long additionalTimeInSeconds) {
        if (!whitelistConfig.contains(playerName)) {
            throw new IllegalArgumentException("Player " + playerName + " is not on the whitelist.");
        }

        // Get the old expiry time and duration
        long oldExpiryTime = whitelistConfig.getLong(playerName);
        long oldDuration = whitelistConfig.getLong(playerName + ".duration");

        // Calculate the new duration and expiry time
        long newDuration = oldDuration + additionalTimeInSeconds;
        long newExpiryTime = System.currentTimeMillis() / 1000 + newDuration;

        // Update the duration and expiry time in the whitelist config
        whitelistConfig.set(playerName, newExpiryTime);
        whitelistConfig.set(playerName + ".duration", newDuration);
        saveWhitelist();

        // Cancel the old task
        BukkitRunnable oldTask = tasks.get(playerName);
        if (oldTask != null) {
            oldTask.cancel();
        }

        // Schedule a new task to remove the player from the whitelist after the new duration
        BukkitRunnable newTask = new BukkitRunnable() {
            @Override
            public void run() {
                removePlayerFromWhitelist(playerName);
                if (Bukkit.getOfflinePlayer(playerName).isOnline()) {
                    Bukkit.getOfflinePlayer(playerName).getPlayer().sendMessage("Your time on the whitelist has expired!");
                }
            }
        };
        newTask.runTaskLater(plugin, newDuration * 20);
        tasks.put(playerName, newTask);
    }


    public void removeTimeFromWhitelist(String playerName, long timeToRemoveInSeconds) {
        if (!whitelistConfig.contains(playerName)) {
            throw new IllegalArgumentException("Player " + playerName + " is not on the whitelist.");
        }

        // Get the old expiry time and duration
        long oldExpiryTime = whitelistConfig.getLong(playerName);
        long oldDuration = whitelistConfig.getLong(playerName + ".duration");

        // Calculate the new duration and expiry time
        long newDuration = oldDuration - timeToRemoveInSeconds;
        if (newDuration < 0) {
            throw new IllegalArgumentException("Cannot remove more time than player " + playerName + " has on the whitelist.");
        }
        long newExpiryTime = System.currentTimeMillis() / 1000 + newDuration;

        // Update the duration and expiry time in the whitelist config
        whitelistConfig.set(playerName, newExpiryTime);
        whitelistConfig.set(playerName + ".duration", newDuration);
        saveWhitelist();

        // Cancel the old task
        BukkitRunnable oldTask = tasks.get(playerName);
        if (oldTask != null) {
            oldTask.cancel();
        }

        // Schedule a new task to remove the player from the whitelist after the new duration
        BukkitRunnable newTask = new BukkitRunnable() {
            @Override
            public void run() {
                removePlayerFromWhitelist(playerName);
                if (Bukkit.getOfflinePlayer(playerName).isOnline()) {
                    Bukkit.getOfflinePlayer(playerName).getPlayer().sendMessage("Your time on the whitelist has expired!");
                }
            }
        };
        newTask.runTaskLater(plugin, newDuration * 20);
        tasks.put(playerName, newTask);
    }

    public void removePlayerFromWhitelist(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        player.setWhitelisted(false);
        whitelistConfig.set(playerName, null);
        saveWhitelist();

        BukkitRunnable task = tasks.remove(playerName);
        if (task != null) {
            task.cancel();
        }
    }

    public void loadWhitelist() {
        for (String playerName : whitelistConfig.getKeys(false)) {
            if (!whitelistConfig.isSet(playerName + ".duration")) {
                continue;
            }

            long addedAt = whitelistConfig.getLong(playerName + ".addedAt");
            long duration = whitelistConfig.getLong(playerName + ".duration");
            long remainingTime = addedAt + duration - System.currentTimeMillis() / 1000;

            if (remainingTime > 0) {
                addPlayerToWhitelist(playerName, remainingTime);
            } else {
                removePlayerFromWhitelist(playerName);
            }
        }
    }


    public void saveWhitelist() {
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
