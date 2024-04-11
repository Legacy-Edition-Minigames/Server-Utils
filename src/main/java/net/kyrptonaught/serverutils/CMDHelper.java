package net.kyrptonaught.serverutils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class CMDHelper {

    public static void executeAs(PlayerEntity player, String cmd) {
        player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(2).withSilent(), cmd);
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
