package me.spoony.playtimecommand.utils;

import me.spoony.playtimecommand.PlaytimeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;

public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlaytimeCommand Updater");

    public static String latestVersion;
    public static String modrinthLink = "https://modrinth.com/project/okeAxC3Y/versions";

    private static final String CURRENT_VERSION = PlaytimeCommand.getCurrentVersion();
    private static final String GITHUB_PROPERTIES_URL = "https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/master/gradle.properties";

    public static void checkUpdate() {

        try {
            Properties prop = new Properties();
            prop.load(new URL(GITHUB_PROPERTIES_URL).openStream());
            latestVersion = prop.getProperty("mod_version");

            if (latestVersion == null || latestVersion.equals("0")) {
                LOGGER.warn("[Playtime Command] Version checker returned null or 0. Version checker disabled.");
                return;
            }

            if (!CURRENT_VERSION.equals(latestVersion)) {
                LOGGER.warn("[Playtime Command] A newer version {} is available! Please consider updating!", latestVersion);
                LOGGER.warn("[Playtime Command] Current version: {}", CURRENT_VERSION);
                LOGGER.warn("[Playtime Command] Download here: {}", modrinthLink);
            } else {
                LOGGER.info("[Playtime Command] Already on latest version ({})", latestVersion);
            }
        } catch (Exception e) {
            LOGGER.error("[Playtime Command] Failed to check version. Assuming we're on latest version.", e);
        }
    }
}