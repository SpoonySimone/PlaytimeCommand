package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class HelpCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component helpMessage = Component.literal("Playtime Command Help\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("/playtime - See your own playtime\n"))
                .append(Component.literal("/playtime ").append(Component.literal("<username>").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See another online player's playtime\n")))
                .append(Component.literal("/playtime ").append(Component.literal("--top").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See top 10 players by playtime\n")))
                .append(Component.literal("/playtime ").append(Component.literal("--about").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See mod information\n")))
                .append(Component.literal("/playtime ").append(Component.literal("--help").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(" - See command usage")));

        source.sendSuccess(() -> helpMessage, false);
        return 1;
    }
}