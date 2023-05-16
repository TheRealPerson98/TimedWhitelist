package com.person98.extendedwhitelist;

import com.person98.extendedwhitelist.commands.WhitelistCommand;
import com.person98.extendedwhitelist.managers.WhitelistManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExtendedWhitelist extends JavaPlugin {

    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        whitelistManager = new WhitelistManager(this);
        whitelistManager.loadWhitelist();
        getCommand("ExtendedWhitelist").setExecutor(new WhitelistCommand(this, whitelistManager));
    }

    @Override
    public void onDisable() {
        whitelistManager.saveWhitelist();
    }
}
