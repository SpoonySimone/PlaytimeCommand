package me.spoony.playtimeviewer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaytimeViewer implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Playtime Viewer");

    @Override
    public void onInitialize() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("[Playtime Viewer] Initialization started");

        // Register the playtime command
        CommandRegistrationCallback.EVENT.register(PlaytimeViewerCommand::register);
        LOGGER.info("[Playtime Viewer] Registered command");

        // Register shutdown event
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[Playtime Viewer] Shutting down");
        });

        long endTime = System.currentTimeMillis();
        LOGGER.info("[Playtime Viewer] Mod startup completed in {}ms", endTime - startTime);
    }
}