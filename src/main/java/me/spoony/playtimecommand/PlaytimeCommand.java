package me.spoony.playtimecommand;

import me.spoony.playtimecommand.command.PlaytimeCommandCommand;
import me.spoony.playtimecommand.utils.UpdateChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaytimeCommand implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Playtime Command");

    @Override
    public void onInitialize() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("[Playtime Command] Initialization started");

        // Register the playtime command
        CommandRegistrationCallback.EVENT.register(PlaytimeCommandCommand::register);
        LOGGER.info("[Playtime Command] Registered command");

        UpdateChecker.checkUpdate();

        // Register shutdown event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[Playtime Command] Shutting down");
        });

        long endTime = System.currentTimeMillis();
        LOGGER.info("[Playtime Command] Mod startup completed in {}ms", endTime - startTime);
    }

    public static String getCurrentVersion() {
        try {
            return FabricLoader.getInstance()
                    .getModContainer("playtimecommand")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");
        } catch (Exception e) {
            LOGGER.warn("[Playtime Command] Could not load current version from Fabric loader, using fallback", e);
            return "unknown";
        }
    }
}