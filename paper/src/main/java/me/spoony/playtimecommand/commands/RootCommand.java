package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.spoony.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class RootCommand {

    private static final SuggestionProvider<CommandSourceStack> ONLINE_PLAYERS = (context, builder) -> {
        context.getSource().getSender().getServer().getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .forEach(builder::suggest);
        return builder.buildFuture();
    };

    public static void register(Commands registrar) {
        registrar.register(
                Commands.literal("playtime")
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
                                .executes(HelpCommand::execute))
                        .build(),
                "See your playtime or other players' playtime"
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Please specify a player: /playtime <player>").color(NamedTextColor.RED));
            return 0;
        }

        return printPlaytime(player, sender, true);
    }

    private static int executeOtherPlayer(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        try {
            String playerName = StringArgumentType.getString(context, "player");
            Player targetPlayer = sender.getServer().getPlayer(playerName);

            if (targetPlayer == null) {
                sender.sendMessage(Component.text("Player '" + playerName + "' not found!").color(NamedTextColor.RED));
                return 0;
            }

            boolean isSelf = (sender instanceof Player selfPlayer) && selfPlayer.equals(targetPlayer);
            return printPlaytime(targetPlayer, sender, isSelf);
        } catch (Exception e) {
            sender.sendMessage(Component.text("An error occurred while executing the command!").color(NamedTextColor.RED));
            return 0;
        }
    }

    private static int printPlaytime(Player player, CommandSender source, boolean isSelf) {
        int playtimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        String formattedTime = formatPlaytime(playtimeTicks);

        Component message = Component.text()
                .append(Component.text(isSelf ? "Your playtime is " : player.getName() + "'s playtime is ")
                        .color(NamedTextColor.GOLD))
                .append(Component.text(formattedTime)
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .build();

        source.sendMessage(message);
        return 1;
    }
}