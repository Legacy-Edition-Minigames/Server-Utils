package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ChatDisablerAction {

    public static int chatDisable(String source, boolean enabled, Consumer<Text> responder) {
        ProhibitorModule.config().globalChatEnabled = enabled;

        Integrations.chatDisabler(source, ProhibitorModule.config().globalChatEnabled);
        ServerUtilsMod.ProhibitorModule.saveConfig();
        if (ProhibitorModule.config().globalChatEnabled) {
            responder.accept(Text.translatable("chatdisabler.enablechat.enabled"));
        } else
            responder.accept(Text.translatable("chatdisabler.enablechat.disabled"));
        return 1;
    }
}
