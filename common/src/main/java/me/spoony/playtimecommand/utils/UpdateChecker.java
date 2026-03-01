package me.spoony.playtimecommand.utils;

import java.net.URI;
import java.util.Properties;
import java.util.function.Consumer;

public class UpdateChecker {
    public static String latestVersion;
    public static String modrinthLink = "https://modrinth.com/project/okeAxC3Y/versions";

    private static final String GITHUB_PROPERTIES_URL = "https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/master/gradle.properties";

    public static void checkUpdate(Consumer<String> warn, Consumer<String> error, String currentVersion) {
        try {
            Properties prop = new Properties();
            prop.load(URI.create(GITHUB_PROPERTIES_URL).toURL().openStream());
            latestVersion = prop.getProperty("mod_version");

            if (latestVersion == null || latestVersion.equals("0")) {
                warn.accept("Version checker returned null or 0. Version checker disabled.");
                return;
            }

            if (!currentVersion.equals(latestVersion)) {
                warn.accept("A newer version " + latestVersion + " is available! Please consider updating!");
                warn.accept("Current version: " + currentVersion);
                warn.accept("Download here: " + modrinthLink);
            }
        } catch (Exception e) {
            error.accept("Failed to check version. Assuming we're on latest version. " + e.getMessage());
        }
    }
}