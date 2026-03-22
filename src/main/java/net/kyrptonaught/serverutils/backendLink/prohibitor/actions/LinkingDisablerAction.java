package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;


import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class LinkingDisablerAction {

    public static int linkDisabler(String source, boolean enabled, Consumer<Text> responder) {
        ProhibitorModule.config().linkingEnabled = enabled;

        Integrations.linkingStatus(source, ProhibitorModule.config().linkingEnabled);
        ServerUtilsMod.ProhibitorModule.saveConfig();

        responder.accept(Text.literal("Linking set to: " + ProhibitorModule.config().linkingEnabled));
        return 1;
    }
}
