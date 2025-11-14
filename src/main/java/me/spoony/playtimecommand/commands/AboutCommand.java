package me.spoony.playtimecommand.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import me.spoony.playtimecommand.PlaytimeCommand;

public class AboutCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
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
}