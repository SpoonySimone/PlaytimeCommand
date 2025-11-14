package me.spoony.playtimecommand.commands;

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
import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class RootCommand {

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
                .executes(RootCommand::execute)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(ONLINE_PLAYERS)
                        .executes(RootCommand::executeOtherPlayer))
                .then(Commands.literal("--top")
                        .executes(TopCommand::execute)
                        .then(Commands.argument("page", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                .executes(TopCommand::execute)))
                .then(Commands.literal("--about")
                        .executes(AboutCommand::execute))
                .then(Commands.literal("--help")
                        .executes(HelpCommand::execute)));
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
            return printPlaytime(player, source, true);
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

            return printPlaytime(targetPlayer, source, source.isPlayer() && source.getPlayer() == targetPlayer);
        } catch (Exception e) {
            source.sendFailure(Component.literal("An error occurred while executing the command!"));
            return 0;
        }
    }

    private static int printPlaytime(ServerPlayer player, CommandSourceStack source, boolean isSelf) {
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