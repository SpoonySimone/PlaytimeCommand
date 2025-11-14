package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class TopCommand {
    private static final Logger LOGGER = LogManager.getLogger(TopCommand.class);

    private static final int PLAYERS_PER_PAGE = 10;
    private static final long CACHE_DURATION_MS = 3000; // 3 seconds
    private static final long LOADING_MESSAGE_DELAY_MS = 500;

    private static List<PlayerPlaytime> cachedPlayerList = null;
    private static long lastCacheTime = 0;

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // try to get page number from arguments, default to 1 if not provided
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {}

        final int requestedPage = page;
        long currentTime = System.currentTimeMillis();
        if (cachedPlayerList != null && (currentTime - lastCacheTime) < CACHE_DURATION_MS) {
            Component leaderboard = buildLeaderboardPage(cachedPlayerList, requestedPage);
            source.sendSuccess(() -> leaderboard, false);
            return 1;
        }

        AtomicBoolean calculationComplete = new AtomicBoolean(false);

        // send loading message if it has been more than a sec
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(LOADING_MESSAGE_DELAY_MS);
                if (!calculationComplete.get()) {
                    source.sendSuccess(() -> Component.literal("Calculating playtime leaderboard...")
                            .withStyle(ChatFormatting.YELLOW), false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // calculate leaderboard asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                List<PlayerPlaytime> playerList = calculatePlayerList(source);

                // cache result
                cachedPlayerList = playerList;
                lastCacheTime = System.currentTimeMillis();
                calculationComplete.set(true);

                // build and print result for requested page
                Component leaderboard = buildLeaderboardPage(playerList, requestedPage);
                source.sendSuccess(() -> leaderboard, false);
            } catch (Exception e) {
                calculationComplete.set(true);
                source.sendFailure(Component.literal("An error occurred while calculating the leaderboard!"));
                LOGGER.error("Error calculating leaderboard", e);
            }
        });

        return 1;
    }

    private static List<PlayerPlaytime> calculatePlayerList(CommandSourceStack source) {
        List<ServerPlayer> players = source.getServer().getPlayerList().getPlayers();

        return players.stream()
                .map(player -> {
                    int playtimeTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
                    return new PlayerPlaytime(player.getScoreboardName(), playtimeTicks);
                })
                .sorted(Comparator.comparingInt(PlayerPlaytime::playtimeTicks).reversed())
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

            leaderboard = leaderboard
                    .append(Component.literal(position + ". ")
                            .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(pp.playerName())
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" - ")
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(formattedTime)
                            .withStyle(ChatFormatting.YELLOW));

            if (i < endIndex - 1) {
                leaderboard = leaderboard.append(Component.literal("\n"));
            }
        }

        // page navigation (if there are multiple pages)
        if (totalPages > 1) {
            leaderboard = leaderboard.append(Component.literal("\n"));

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
                navigation = navigation.append(Component.literal(" << Prev").setStyle(prevStyle));
            } else {
                navigation = navigation.append(Component.literal(" << Prev").withStyle(ChatFormatting.DARK_GRAY));
            }

            navigation = navigation.append(Component.literal(" " + finalPage + "/" + totalPages + " ").withStyle(ChatFormatting.GRAY));

            // next page button
            if (finalPage < totalPages) {
                net.minecraft.network.chat.Style nextStyle = net.minecraft.network.chat.Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new net.minecraft.network.chat.ClickEvent.RunCommand("/playtime --top " + (finalPage + 1)))
                        .withHoverEvent(new net.minecraft.network.chat.HoverEvent.ShowText(
                                Component.literal("Go to page " + (finalPage + 1)).withStyle(ChatFormatting.GREEN)
                        ));
                navigation = navigation.append(Component.literal("Next >> ").setStyle(nextStyle));
            } else {
                navigation = navigation.append(Component.literal("Next >> ").withStyle(ChatFormatting.DARK_GRAY));
            }

            navigation = navigation.append(Component.literal("━━━━").withStyle(ChatFormatting.GOLD));

            leaderboard = leaderboard.append(navigation);
        }

        return leaderboard;
    }

    private record PlayerPlaytime(String playerName, int playtimeTicks) {}
}