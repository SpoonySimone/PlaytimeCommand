package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class TopCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger("Playtime Command");

    private static final int PLAYERS_PER_PAGE = 10;
    private static final long CACHE_DURATION_MS = 2000; // 2 seconds
    private static final long LOADING_MESSAGE_DELAY_MS = 500;

    private static List<PlayerPlaytime> cachedPlayerList = null;
    private static long lastCacheTime = 0;
    private static boolean isCalculating = false;
    private static final List<PendingRequest> pendingRequests = new ArrayList<>();

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // try to get page number from arguments, default to 1 if not provided
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {}

        final int requestedPage = page;
        if (cachedPlayerList != null && (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION_MS) {
            Component leaderboard = buildLeaderboardPage(cachedPlayerList, requestedPage);
            source.sendSuccess(() -> leaderboard, false);
            return 1;
        }

        pendingRequests.add(new PendingRequest(source, requestedPage));
        if (isCalculating) return 1;

        // send loading message if it has been more than a sec
        isCalculating = true;
        List<PlayerPlaytime> snapshot = snapshotPlayerData(source);
        MinecraftServer server = source.getServer();

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(LOADING_MESSAGE_DELAY_MS);
                if (isCalculating) {
                    server.execute(() -> {
                        for (PendingRequest req : pendingRequests) {
                            req.source().sendSuccess(() -> Component.literal("Calculating playtime leaderboard...")
                                    .withStyle(ChatFormatting.YELLOW), false);
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
                server.execute(() -> {
                    cachedPlayerList = sorted;
                    lastCacheTime = System.currentTimeMillis();
                    isCalculating = false;
                    List<PendingRequest> toServe = new ArrayList<>(pendingRequests);
                    pendingRequests.clear();
                    for (PendingRequest req : toServe) {
                        Component leaderboard = buildLeaderboardPage(sorted, req.page());
                        req.source().sendSuccess(() -> leaderboard, false);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error calculating leaderboard", e);
                server.execute(() -> {
                    isCalculating = false;
                    for (PendingRequest req : pendingRequests) {
                        req.source().sendFailure(Component.literal("An error occurred while calculating the leaderboard!"));
                    }
                    pendingRequests.clear();
                });
            }
        });

        return 1;
    }

    private static List<PlayerPlaytime> snapshotPlayerData(CommandSourceStack source) {
        return source.getServer().getPlayerList().getPlayers().stream()
                .map(player -> new PlayerPlaytime(
                        player.getScoreboardName(),
                        player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME))))
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
        MutableComponent leaderboard = Component.literal("━━━━━━ ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Playtime Leaderboard")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" ━━━━━━\n")
                        .withStyle(ChatFormatting.GOLD));

        for (int i = startIndex; i < endIndex; i++) {
            PlayerPlaytime pp = playerList.get(i);
            int position = i + 1;
            String formattedTime = formatPlaytime(pp.playtimeTicks());

            // remove period from playtime since its cleaner
            if (formattedTime.endsWith(".")) {
                formattedTime = formattedTime.substring(0, formattedTime.length() - 1);
            }

            leaderboard
                    .append(Component.literal(position + ". ")
                            .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(pp.playerName())
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" - ")
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(formattedTime)
                            .withStyle(ChatFormatting.YELLOW));

            if (i < endIndex - 1) {
                leaderboard.append(Component.literal("\n"));
            }
        }

        // page navigation (if there are multiple pages)
        if (totalPages > 1) {
            leaderboard.append(Component.literal("\n"));

            MutableComponent navigation = Component.literal("━━━━")
                    .withStyle(ChatFormatting.GOLD);

            // add buttons
            // previous page button
            if (finalPage > 1) {
                net.minecraft.network.chat.Style prevStyle = net.minecraft.network.chat.Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand("/playtime --top " + (finalPage - 1)))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(
                                Component.literal("Go to page " + (finalPage - 1)).withStyle(ChatFormatting.GREEN)
                        ));
                navigation.append(Component.literal(" << Prev").setStyle(prevStyle));
            } else {
                navigation.append(Component.literal(" << Prev").withStyle(ChatFormatting.DARK_GRAY));
            }

            navigation.append(Component.literal(" " + finalPage + "/" + totalPages + " ").withStyle(ChatFormatting.GRAY));

            // next page button
            if (finalPage < totalPages) {
                net.minecraft.network.chat.Style nextStyle = net.minecraft.network.chat.Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand("/playtime --top " + (finalPage + 1)))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(
                                Component.literal("Go to page " + (finalPage + 1)).withStyle(ChatFormatting.GREEN)
                        ));
                navigation.append(Component.literal("Next >> ").setStyle(nextStyle));
            } else {
                navigation.append(Component.literal("Next >> ").withStyle(ChatFormatting.DARK_GRAY));
            }

            navigation.append(Component.literal("━━━━").withStyle(ChatFormatting.GOLD));

            leaderboard.append(navigation);
        }

        return leaderboard;
    }

    private record PlayerPlaytime(String playerName, int playtimeTicks) {}
    private record PendingRequest(CommandSourceStack source, int page) {}
}