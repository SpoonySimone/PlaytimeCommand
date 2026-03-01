package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.spoony.playtimecommand.PlaytimeCommandPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class AboutCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        Component aboutMessage = Component.text()
                .append(Component.text("Playtime Command")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" " + PlaytimeCommandPlugin.getCurrentVersion())
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" by ")
                        .color(NamedTextColor.GOLD))
                .append(Component.text("SpoonySimone")
                        .color(NamedTextColor.DARK_GREEN))
                .build();

        context.getSource().getSender().sendMessage(aboutMessage);
        return 1;
    }
}