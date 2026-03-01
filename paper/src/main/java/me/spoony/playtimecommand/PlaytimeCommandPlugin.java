package me.spoony.playtimecommand;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.spoony.playtimecommand.commands.RootCommand;
import me.spoony.playtimecommand.utils.UpdateChecker;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaytimeCommandPlugin extends JavaPlugin {

    private static PlaytimeCommandPlugin instance;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;

        getLogger().info("Initialization started");

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            RootCommand.register(event.registrar());
            getLogger().info("Registered command");
            getLogger().info("Plugin startup completed in " + (System.currentTimeMillis() - startTime) + "ms");
        });

        UpdateChecker.checkUpdate(getLogger()::warning, getLogger()::severe, getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down");
    }

    public static PlaytimeCommandPlugin getInstance() {
        return instance;
    }

    public static String getCurrentVersion() {
        if (instance == null) return "unknown";
        return instance.getPluginMeta().getVersion();
    }
}