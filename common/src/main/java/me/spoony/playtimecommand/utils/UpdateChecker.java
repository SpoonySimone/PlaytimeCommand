package me.spoony.playtimecommand.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Properties;

public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlaytimeCommand Updater");

    public static String latestVersion;
    public static String modrinthLink = "https://modrinth.com/project/okeAxC3Y/versions";

    private static final String GITHUB_PROPERTIES_URL = "https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/master/gradle.properties";

    public static void checkUpdate(String currentVersion) {
        try {
            Properties prop = new Properties();
            prop.load(URI.create(GITHUB_PROPERTIES_URL).toURL().openStream());
            latestVersion = prop.getProperty("mod_version");

            if (latestVersion == null || latestVersion.equals("0")) {
                LOGGER.warn("[Playtime Command] Version checker returned null or 0. Version checker disabled.");
                return;
            }

            if (!currentVersion.equals(latestVersion)) {
                LOGGER.warn("[Playtime Command] A newer version {} is available! Please consider updating!", latestVersion);
                LOGGER.warn("[Playtime Command] Current version: {}", currentVersion);
                LOGGER.warn("[Playtime Command] Download here: {}", modrinthLink);
            }
        } catch (Exception e) {
            LOGGER.error("[Playtime Command] Failed to check version. Assuming we're on latest version.", e);
        }
    }
}