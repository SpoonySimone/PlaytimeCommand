package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;

public class HelpCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component helpMessage = Component.literal("Playtime Command Help\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("/playtime")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime").withStyle(ChatFormatting.GREEN))))
                        .append(Component.literal(" - See your own playtime\n").withStyle(ChatFormatting.GOLD)))
                .append(Component.literal("/playtime ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.SuggestCommand("/playtime "))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to fill /playtime").withStyle(ChatFormatting.GREEN))))
                        .append(Component.literal("<username>").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)
                                .withClickEvent(new ClickEvent.SuggestCommand("/playtime "))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to fill /playtime").withStyle(ChatFormatting.GREEN)))))
                        .append(Component.literal(" - See another online player's playtime\n").withStyle(ChatFormatting.GOLD)))
                .append(Component.literal("/playtime ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --top"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --top").withStyle(ChatFormatting.GREEN))))
                        .append(Component.literal("--top").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --top"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --top").withStyle(ChatFormatting.GREEN)))))
                        .append(Component.literal(" - See top 10 online players by playtime\n").withStyle(ChatFormatting.GOLD)))
                .append(Component.literal("/playtime ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --about"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --about").withStyle(ChatFormatting.GREEN))))
                        .append(Component.literal("--about").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --about"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --about").withStyle(ChatFormatting.GREEN)))))
                        .append(Component.literal(" - See mod information\n").withStyle(ChatFormatting.GOLD)))
                .append(Component.literal("/playtime ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --help"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --help").withStyle(ChatFormatting.GREEN))))
                        .append(Component.literal("--help").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN)
                                .withClickEvent(new ClickEvent.RunCommand("/playtime --help"))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to run /playtime --help").withStyle(ChatFormatting.GREEN)))))
                        .append(Component.literal(" - See command usage").withStyle(ChatFormatting.GOLD)));

        source.sendSuccess(() -> helpMessage, false);
        return 1;
    }
}