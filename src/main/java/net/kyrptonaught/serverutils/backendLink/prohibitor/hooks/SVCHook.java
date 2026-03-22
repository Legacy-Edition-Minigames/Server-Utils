package net.kyrptonaught.serverutils.backendLink.prohibitor.hooks;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.minecraft.server.network.ServerPlayerEntity;

public class SVCHook implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return ServerUtilsMod.ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onPacket);
    }

    private void onPacket(MicrophonePacketEvent microphonePacketEvent) {
        if (!ProhibitorModule.canChat((ServerPlayerEntity) microphonePacketEvent.getSenderConnection().getPlayer().getPlayer())) {
            microphonePacketEvent.cancel();
        }
    }
}
