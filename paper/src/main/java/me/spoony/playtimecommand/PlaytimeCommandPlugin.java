package me.spoony.playtimecommand;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.spoony.playtimecommand.commands.RootCommand;
import me.spoony.playtimecommand.utils.UpdateChecker;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaytimeCommandPlugin extends JavaPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("Playtime Command");
    private static PlaytimeCommandPlugin instance;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;

        LOGGER.info("[Playtime Command] Initialization started");

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            RootCommand.register(event.registrar());
            LOGGER.info("[Playtime Command] Registered command");
        });

        UpdateChecker.checkUpdate(getPluginMeta().getVersion());

        long endTime = System.currentTimeMillis();
        LOGGER.info("[Playtime Command] Plugin startup completed in {}ms", endTime - startTime);
    }

    @Override
    public void onDisable() {
        LOGGER.info("[Playtime Command] Shutting down");
    }

    public static PlaytimeCommandPlugin getInstance() {
        return instance;
    }

    //get current plugin version
    public static String getCurrentVersion() {
        if (instance == null) return "unknown";
        return instance.getPluginMeta().getVersion();
    }
}