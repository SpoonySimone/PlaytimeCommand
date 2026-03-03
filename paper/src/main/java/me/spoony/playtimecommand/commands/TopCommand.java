package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.spoony.playtimecommand.PlaytimeCommandPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class TopCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopCommand.class);

    private static final int PLAYERS_PER_PAGE = 10;
    private static final long CACHE_DURATION_MS = 2000; // 2 seconds
    private static final long LOADING_MESSAGE_DELAY_MS = 500;

    private static List<PlayerPlaytime> cachedPlayerList = null;
    private static long lastCacheTime = 0;
    private static boolean isCalculating = false;
    private static final List<PendingRequest> pendingRequests = new ArrayList<>();

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        // try to get page number from arguments, default to 1 if not provided
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {}

        final int requestedPage = page;
        if (cachedPlayerList != null && (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION_MS) {
            sender.sendMessage(buildLeaderboardPage(cachedPlayerList, requestedPage));
            return 1;
        }

        pendingRequests.add(new PendingRequest(sender, requestedPage));
        if (isCalculating) return 1;

        // send loading message if it has been more than a sec
        isCalculating = true;
        List<PlayerPlaytime> snapshot = snapshotPlayerData(sender);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(LOADING_MESSAGE_DELAY_MS);
                if (isCalculating) {
                    Bukkit.getScheduler().runTask(PlaytimeCommandPlugin.getInstance(), () -> {
                        for (PendingRequest req : pendingRequests) {
                            req.sender().sendMessage(Component.text("Calculating playtime leaderboard...")
                                    .color(NamedTextColor.YELLOW));
                        }
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // calculate leaderboard asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                List<PlayerPlaytime> sorted = snapshot.stream()
                        .sorted(Comparator.comparingInt(PlayerPlaytime::playtimeTicks).reversed())
                        .toList();
                Bukkit.getScheduler().runTask(PlaytimeCommandPlugin.getInstance(), () -> {
                    cachedPlayerList = sorted;
                    lastCacheTime = System.currentTimeMillis();
                    isCalculating = false;
                    List<PendingRequest> toServe = new ArrayList<>(pendingRequests);
                    pendingRequests.clear();
                    for (PendingRequest req : toServe) {
                        req.sender().sendMessage(buildLeaderboardPage(sorted, req.page()));
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error calculating leaderboard", e);
                Bukkit.getScheduler().runTask(PlaytimeCommandPlugin.getInstance(), () -> {
                    isCalculating = false;
                    for (PendingRequest req : pendingRequests) {
                        req.sender().sendMessage(Component.text("An error occurred while calculating the leaderboard!")
                                .color(NamedTextColor.RED));
                    }
                    pendingRequests.clear();
                });
            }
        });

        return 1;
    }

    private static List<PlayerPlaytime> snapshotPlayerData(CommandSender sender) {
        return sender.getServer().getOnlinePlayers().stream()
                .map(player -> new PlayerPlaytime(
                        player.getName(),
                        player.getStatistic(Statistic.PLAY_ONE_MINUTE)))
                .toList();
    }

    private static Component buildLeaderboardPage(List<PlayerPlaytime> playerList, int page) {
        int totalPlayers = playerList.size();
        int totalPages = (int) Math.ceil((double) totalPlayers / PLAYERS_PER_PAGE);

        // validate page number
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

        final int finalPage = page;

        int startIndex = (finalPage - 1) * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, totalPlayers);

        // build the leaderboard message
        // header
        TextComponent.Builder leaderboard = Component.text()
                .append(Component.text("━━━━━━ Playtime Leaderboard ━━━━━━")
                        .color(NamedTextColor.GOLD))
                .append(Component.newline());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerPlaytime pp = playerList.get(i);
            int position = i + 1;
            String formattedTime = formatPlaytime(pp.playtimeTicks());

            // remove period from playtime since its cleaner
            if (formattedTime.endsWith(".")) {
                formattedTime = formattedTime.substring(0, formattedTime.length() - 1);
            }

            leaderboard
                    .append(Component.text(position + ". ")
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(pp.playerName())
                            .color(NamedTextColor.GREEN))
                    .append(Component.text(" - ")
                            .color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(formattedTime)
                            .color(NamedTextColor.YELLOW));

            if (i < endIndex - 1) {
                leaderboard.append(Component.newline());
            }
        }

        // page navigation (if there are multiple pages)
        if (totalPages > 1) {
            leaderboard.append(Component.newline());

            TextComponent.Builder navigation = Component.text()
                    .append(Component.text("━━━━")
                            .color(NamedTextColor.GOLD));

            // add buttons
            // previous page button
            if (finalPage > 1) {
                navigation.append(Component.text(" << Prev")
                        .color(NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/playtime --top " + (finalPage - 1)))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Go to page " + (finalPage - 1)).color(NamedTextColor.GREEN)
                        )));
            } else {
                navigation.append(Component.text(" << Prev").color(NamedTextColor.DARK_GRAY));
            }

            navigation.append(Component.text(" " + finalPage + "/" + totalPages + " ").color(NamedTextColor.GRAY));

            // next page button
            if (finalPage < totalPages) {
                navigation.append(Component.text("Next >> ")
                        .color(NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/playtime --top " + (finalPage + 1)))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Go to page " + (finalPage + 1)).color(NamedTextColor.GREEN)
                        )));
            } else {
                navigation.append(Component.text("Next >> ").color(NamedTextColor.DARK_GRAY));
            }

            navigation.append(Component.text("━━━━").color(NamedTextColor.GOLD));

            leaderboard.append(navigation);
        }

        return leaderboard.build();
    }

    private record PlayerPlaytime(String playerName, int playtimeTicks) {}
    private record PendingRequest(CommandSender sender, int page) {}
}