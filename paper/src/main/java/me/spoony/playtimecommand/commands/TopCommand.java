package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

@SuppressWarnings("UnstableApiUsage")
public class TopCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopCommand.class);

    private static final int PLAYERS_PER_PAGE = 10;
    private static final long CACHE_DURATION_MS = 3000;
    private static final long LOADING_MESSAGE_DELAY_MS = 500;

    private static List<PlayerPlaytime> cachedPlayerList = null;
    private static long lastCacheTime = 0;

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {}

        final int requestedPage = page;
        long currentTime = System.currentTimeMillis();
        if (cachedPlayerList != null && (currentTime - lastCacheTime) < CACHE_DURATION_MS) {
            sender.sendMessage(buildLeaderboardPage(cachedPlayerList, requestedPage));
            return 1;
        }

        AtomicBoolean calculationComplete = new AtomicBoolean(false);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(LOADING_MESSAGE_DELAY_MS);
                if (!calculationComplete.get()) {
                    sender.sendMessage(Component.text("Calculating playtime leaderboard...").color(NamedTextColor.YELLOW));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        CompletableFuture.runAsync(() -> {
            try {
                List<PlayerPlaytime> playerList = calculatePlayerList(sender);

                cachedPlayerList = playerList;
                lastCacheTime = System.currentTimeMillis();
                calculationComplete.set(true);

                sender.sendMessage(buildLeaderboardPage(playerList, requestedPage));
            } catch (Exception e) {
                calculationComplete.set(true);
                sender.sendMessage(Component.text("An error occurred while calculating the leaderboard!").color(NamedTextColor.RED));
                LOGGER.error("Error calculating leaderboard", e);
            }
        });

        return 1;
    }

    private static List<PlayerPlaytime> calculatePlayerList(CommandSender sender) {
        Collection<? extends Player> players = sender.getServer().getOnlinePlayers();

        return players.stream()
                .map(player -> {
                    int playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
                    return new PlayerPlaytime(player.getName(), playtimeTicks);
                })
                .sorted(Comparator.comparingInt(PlayerPlaytime::playtimeTicks).reversed())
                .toList();
    }

    private static Component buildLeaderboardPage(List<PlayerPlaytime> playerList, int page) {
        int totalPlayers = playerList.size();
        int totalPages = (int) Math.ceil((double) totalPlayers / PLAYERS_PER_PAGE);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        final int finalPage = page;
        int startIndex = (finalPage - 1) * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, totalPlayers);

        TextComponent.Builder leaderboard = Component.text()
                .append(Component.text("━━━━━━ Playtime Leaderboard ━━━━━━").color(NamedTextColor.GOLD))
                .append(Component.newline());

        for (int i = startIndex; i < endIndex; i++) {
            PlayerPlaytime pp = playerList.get(i);
            int position = i + 1;
            String formattedTime = formatPlaytime(pp.playtimeTicks());

            if (formattedTime.endsWith(".")) {
                formattedTime = formattedTime.substring(0, formattedTime.length() - 1);
            }

            leaderboard
                    .append(Component.text(position + ". ").color(NamedTextColor.GOLD))
                    .append(Component.text(pp.playerName()).color(NamedTextColor.GREEN))
                    .append(Component.text(" - ").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(formattedTime).color(NamedTextColor.YELLOW));

            if (i < endIndex - 1) {
                leaderboard.append(Component.newline());
            }
        }

        if (totalPages > 1) {
            leaderboard.append(Component.newline());

            TextComponent.Builder navigation = Component.text().append(Component.text("━━━━").color(NamedTextColor.GOLD));

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
}