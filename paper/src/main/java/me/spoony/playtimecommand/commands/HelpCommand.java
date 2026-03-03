package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        Component helpMessage = Component.text()
                .append(Component.text("Playtime Command Help\n").color(NamedTextColor.GOLD))
                .append(Component.text("/playtime")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/playtime"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime").color(NamedTextColor.GREEN))))
                .append(Component.text(" - See your own playtime\n").color(NamedTextColor.GOLD))
                .append(Component.text("/playtime ")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.suggestCommand("/playtime "))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to fill /playtime").color(NamedTextColor.GREEN))))
                .append(Component.text("<username>")
                        .color(NamedTextColor.DARK_GREEN)
                        .clickEvent(ClickEvent.suggestCommand("/playtime "))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to fill /playtime").color(NamedTextColor.GREEN))))
                .append(Component.text(" - See another online player's playtime\n").color(NamedTextColor.GOLD))
                .append(Component.text("/playtime ")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/playtime --top"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --top").color(NamedTextColor.GREEN))))
                .append(Component.text("--top")
                        .color(NamedTextColor.DARK_GREEN)
                        .clickEvent(ClickEvent.runCommand("/playtime --top"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --top").color(NamedTextColor.GREEN))))
                .append(Component.text(" - See top 10 online players by playtime\n").color(NamedTextColor.GOLD))
                .append(Component.text("/playtime ")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/playtime --about"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --about").color(NamedTextColor.GREEN))))
                .append(Component.text("--about")
                        .color(NamedTextColor.DARK_GREEN)
                        .clickEvent(ClickEvent.runCommand("/playtime --about"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --about").color(NamedTextColor.GREEN))))
                .append(Component.text(" - See mod information\n").color(NamedTextColor.GOLD))
                .append(Component.text("/playtime ")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/playtime --help"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --help").color(NamedTextColor.GREEN))))
                .append(Component.text("--help")
                        .color(NamedTextColor.DARK_GREEN)
                        .clickEvent(ClickEvent.runCommand("/playtime --help"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to run /playtime --help").color(NamedTextColor.GREEN))))
                .append(Component.text(" - See command usage").color(NamedTextColor.GOLD))
                .build();

        context.getSource().getSender().sendMessage(helpMessage);
        return 1;
    }
}