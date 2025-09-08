package me.spoony.playtimecommand.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;
import me.spoony.playtimecommand.PlaytimeCommand;
import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class PlaytimeCommandCommand {

    //only suggest online players
    private static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYERS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
                context.getSource().getServer().getPlayerNames(), builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                net.minecraft.commands.CommandBuildContext registryAccess,
                                Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("playtime")
                .executes(PlaytimeCommandCommand::execute)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS)
                        .executes(PlaytimeCommandCommand::executeOtherPlayer))
                .then(Commands.literal("--about")
                        .executes(PlaytimeCommandCommand::executeAbout))
                .then(Commands.literal("--help")
                        .executes(PlaytimeCommandCommand::executeHelp)));
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
            String playerName = StringArgumentType.getString(context, "player");
            ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

            if (targetPlayer == null) {
                source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
                return 0;
            }

            return showPlaytime(targetPlayer, source, source.isPlayer() && source.getPlayer() == targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.literal("An error occurred while executing the command!"));
            return 0;
        }
    }

    private static int executeAbout(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component aboutMessage = Component.empty()
                .append(Component.literal("Playtime Command")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" " + PlaytimeCommand.getCurrentVersion())
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" by ")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal("SpoonySimone")
                        .withStyle(ChatFormatting.DARK_GREEN));

        source.sendSuccess(() -> aboutMessage, false);
        return 1;
    }

    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component helpMessage = Component.literal("Playtime Command Help\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("/playtime - See your own playtime\n"))
                .append(Component.literal("/playtime ").append(Component.literal("<username>").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See another online player's playtime\n")))
                .append(Component.literal("/playtime ").append(Component.literal("--about").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See mod information\n")))
                .append(Component.literal("/playtime ").append(Component.literal("--help").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See command usage")));

        source.sendSuccess(() -> helpMessage, false);
        return 1;
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
}