package net.kyrptonaught.serverutils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.Supplier;

public class CMDHelper {

    public static void executeAs(PlayerEntity player, String cmd) {
        player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(2).withSilent(), cmd);
    }

    public static void executeAs(PlayerEntity player, Collection<CommandFunction<ServerCommandSource>> functions) {
        for (CommandFunction<ServerCommandSource> commandFunction : functions) {
            player.getServer().getCommandFunctionManager().execute(commandFunction, player.getCommandSource().withLevel(2).withSilent());
        }
    }

    public static Supplier<Text> getFeedbackLiteral(String text) {
        return getFeedback(Text.literal(text));
    }

    public static Supplier<Text> getFeedbackTranslatable(String text) {
        return getFeedback(Text.translatable(text));
    }

    public static Supplier<Text> getFeedback(Text text) {
        return () -> text;
    }
}
