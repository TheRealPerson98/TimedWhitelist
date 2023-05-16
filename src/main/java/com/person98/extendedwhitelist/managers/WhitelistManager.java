package com.person98.extendedwhitelist.managers;

import com.person98.extendedwhitelist.ExtendedWhitelist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WhitelistManager {
    private final ExtendedWhitelist plugin;
    private final File whitelistFile;
    private final FileConfiguration whitelistConfig;
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();
    private FileConfiguration config;

    private boolean kickOnWhitelistEnd;
    private boolean sendExpirationWarnings;
    private JDA jda;
    private String channelId;
    public WhitelistManager(ExtendedWhitelist plugin) {
        this.plugin = plugin;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        this.whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
        this.config = plugin.getConfig();

        this.kickOnWhitelistEnd = config.getBoolean("kickOnWhitelistEnd", true);
        this.sendExpirationWarnings = config.getBoolean("sendExpirationWarnings", true);
        if (config.getBoolean("discordIntegrationEnabled")) {
            try {
                jda = JDABuilder.createDefault(config.getString("discordBotToken"))
                        .enableIntents(GatewayIntent.GUILD_MESSAGES)
                        .build();
                channelId = config.getString("discordChannelId");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void addPlayerToWhitelist(UUID playerUUID, long durationInSeconds) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        player.setWhitelisted(true);
        String uuidString = playerUUID.toString();
        whitelistConfig.set(uuidString, System.currentTimeMillis() / 1000 + durationInSeconds);
        whitelistConfig.set(uuidString + ".duration", durationInSeconds);
        whitelistConfig.set(uuidString + ".addedAt", System.currentTimeMillis() / 1000);

        saveWhitelist();

        BukkitRunnable task = createExpirationTask(playerUUID, durationInSeconds);
        scheduleExpirationWarnings(player, durationInSeconds);

        task.runTaskLater(plugin, durationInSeconds * 20);
        tasks.put(playerUUID, task);

        if (jda != null) {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Whitelist Update");
                embedBuilder.setDescription(player.getName() + " has been added to the whitelist for " + formatDuration(durationInSeconds) + ".");
                MessageEmbed embed = embedBuilder.build();
                embedBuilder.setColor(Color.GREEN); // Set the color to green (you can use other Color constants or specify a custom RGB value)

                channel.sendMessageEmbeds(embed).queue();
            }
        }
    }


    public void addTimeToWhitelist(UUID playerUUID, long additionalTimeInSeconds) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        String uuidString = playerUUID.toString();
        if (!whitelistConfig.contains(uuidString + ".duration")) {
            String expiryMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.whitelistExpired"));
            player.getPlayer().sendMessage(expiryMessage);
        }

        long oldAddedTime = whitelistConfig.getLong(playerUUID + ".addedAt");
        long oldDuration = whitelistConfig.getLong(playerUUID + ".duration");
        long newDuration = oldDuration + additionalTimeInSeconds;

        whitelistConfig.set(playerUUID + ".duration", newDuration);
        saveWhitelist();

        BukkitRunnable oldTask = tasks.get(playerUUID);
        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitRunnable newTask = createExpirationTask(playerUUID, newDuration);
        scheduleExpirationWarnings(player, newDuration);

        newTask.runTaskLater(plugin, newDuration * 20);
        tasks.put(playerUUID, newTask);
    }

    public void removeTimeFromWhitelist(UUID playerUUID, long timeToRemoveInSeconds) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        String uuidString = playerUUID.toString();
        if (!whitelistConfig.contains(uuidString + ".duration")) {
            throw new IllegalArgumentException("Player " + player.getName() + " is not on the whitelist.");
        }

        long oldAddedTime = whitelistConfig.getLong(playerUUID + ".addedAt");
        long oldDuration = whitelistConfig.getLong(playerUUID + ".duration");
        long newDuration = oldDuration - timeToRemoveInSeconds;

        if (newDuration < 0) {
            throw new IllegalArgumentException("Cannot remove more time than player " + player.getName() + " has on the whitelist.");
        }

        whitelistConfig.set(playerUUID + ".duration", newDuration);
        saveWhitelist();

        BukkitRunnable oldTask = tasks.get(playerUUID);
        if (oldTask != null) {
            oldTask.cancel();
        }
        BukkitRunnable newTask = createExpirationTask(playerUUID, newDuration);
        scheduleExpirationWarnings(player, newDuration);

        newTask.runTaskLater(plugin, newDuration * 20);
        tasks.put(playerUUID, newTask);
    }

    public void removePlayerFromWhitelist(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        String uuidString = playerUUID.toString();
        player.setWhitelisted(false);
        whitelistConfig.set(uuidString, null);
        whitelistConfig.set(uuidString + ".duration", null);
        whitelistConfig.set(uuidString + ".addedAt", null);
        saveWhitelist();

        BukkitRunnable task = tasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }

        if (jda != null) {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Whitelist Update");
                embedBuilder.setDescription(player.getName() + "'s whitelist has expired.");
                MessageEmbed embed = embedBuilder.build();
                embedBuilder.setColor(Color.RED); // Set the color to green (you can use other Color constants or specify a custom RGB value)

                channel.sendMessageEmbeds(embed).queue();
            }
        }
    }



    public long getTimeLeft(UUID playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        String uuidString = playerUUID.toString();
        if (!whitelistConfig.contains(uuidString + ".duration")) {
            return -1; // Return -1 or any specific value to indicate that the player is not on the whitelist
        }

        long addedAt = whitelistConfig.getLong(playerUUID + ".addedAt");
        long duration = whitelistConfig.getLong(playerUUID + ".duration");

        return addedAt + duration - System.currentTimeMillis() / 1000;
    }

    public void loadWhitelist() {
        for (String uuidString : whitelistConfig.getKeys(false)) {
            if (!whitelistConfig.isSet(uuidString + ".duration")) {
                continue;
            }

            UUID playerUUID = UUID.fromString(uuidString);

            long addedAt = whitelistConfig.getLong(playerUUID + ".addedAt");
            long duration = whitelistConfig.getLong(playerUUID + ".duration");
            long remainingTime = addedAt + duration - System.currentTimeMillis() / 1000;

            if (remainingTime > 0) {
                addPlayerToWhitelist(playerUUID, remainingTime);
            } else {
                removePlayerFromWhitelist(playerUUID);
            }
        }
    }

    public List<OfflinePlayer> getAllWhitelistedPlayers() {
        return whitelistConfig.getKeys(false).stream()
                .filter(key -> !key.contains("."))
                .map(uuidString -> Bukkit.getOfflinePlayer(UUID.fromString(uuidString)))
                .collect(Collectors.toList());
    }

    public void saveWhitelist() {
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BukkitRunnable createExpirationTask(UUID playerUUID, long durationInSeconds) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                removePlayerFromWhitelist(playerUUID);
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                if (player.isOnline() && kickOnWhitelistEnd) {
                    player.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', config.getString("messages.whitelistExpired")));
                }
            }
        };
    }

    private void scheduleExpirationWarnings(OfflinePlayer player, long durationInSeconds) {
        if (!sendExpirationWarnings) {
            return;
        }

        long fiveMinutes = 5 * 60; // 5 minutes in seconds
        long oneMinute = 1 * 60; // 1 minute in seconds
        long delayFiveMinutes = Math.max(durationInSeconds - fiveMinutes, 0);
        long delayOneMinute = Math.max(durationInSeconds - oneMinute, 0);

        if (delayFiveMinutes > 0) {
            long ticksFiveMinutes = delayFiveMinutes * 20; // Convert seconds to ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.5minleft")));
                }
            }, ticksFiveMinutes);
        }

        if (delayOneMinute > 0) {
            long ticksOneMinute = delayOneMinute * 20; // Convert seconds to ticks
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.1minleft")));
                }
            }, ticksOneMinute);
        }
    }
    public String formatDuration(long durationInSeconds) {
        long days = TimeUnit.SECONDS.toDays(durationInSeconds);
        long hours = TimeUnit.SECONDS.toHours(durationInSeconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.SECONDS.toSeconds(durationInSeconds) - TimeUnit.DAYS.toSeconds(days) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" days ");
        }
        if (hours > 0) {
            sb.append(hours).append(" hours ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" minutes ");
        }
        if (seconds > 0) {
            sb.append(seconds).append(" seconds");
        }
        return sb.toString().trim();
    }

}
