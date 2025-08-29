package me.spoony.playtimeviewer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;

public class PlaytimeViewerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                net.minecraft.commands.CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("playtime")
                .executes(PlaytimeViewerCommand::execute)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(PlaytimeViewerCommand::executeOtherPlayer)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // ask for username if console
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Please specify a player: /playtime <player>"));
            return 0;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            return showPlaytime(player, source, true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("An error occurred while executing the command!"));
            return 0;
        }
    }

    private static int executeOtherPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            return showPlaytime(targetPlayer, source, source.isPlayer() && source.getPlayer() == targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to find the specified player!"));
            return 0;
        }
    }

    private static int showPlaytime(ServerPlayer player, CommandSourceStack source, boolean isSelf) {
        int playtimeTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        String formattedTime = formatPlaytime(playtimeTicks);

        Component message = Component.empty()
                .append(Component.literal(isSelf ? "Your playtime is " : player.getScoreboardName() + "'s playtime is ")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(formattedTime).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        source.sendSuccess(() -> message, false);
        return 1;
    }

    private static String formatPlaytime(int ticks) {
        int totalSeconds = ticks / 20;

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(days == 1 ? " day" : " days");
            if (hours > 0 || minutes > 0 || seconds > 0) {
                result.append(", ");
            }
        }

        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hour" : " hours");
            if (minutes > 0 || seconds > 0) {
                result.append(", ");
            }
        }

        if (minutes > 0) {
            result.append(minutes).append(minutes == 1 ? " minute" : " minutes");
            if (seconds > 0) {
                result.append(" and ");
            }
        }

        if (seconds > 0 || (days == 0 && hours == 0 && minutes == 0)) {
            result.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }
        
        result.append(".");

        return result.toString();
    }
}