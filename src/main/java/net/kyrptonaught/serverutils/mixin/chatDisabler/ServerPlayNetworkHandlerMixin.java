package net.kyrptonaught.serverutils.mixin.chatDisabler;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.chatDisabler.ChatDisabler;
import net.kyrptonaught.serverutils.chatDisabler.ChatDisablerConfig;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onChatMessage", at = @At(value = "HEAD"), cancellable = true)
    public void dontSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (!ChatDisabler.CHATENABLED) {
            ChatDisablerConfig config = ServerUtilsMod.ChatDisabler.getConfig();
            if (config.informClientMSGNotSent)
                this.player.sendMessage(Text.literal(config.disabledResponse), false);
            ci.cancel();
        }
    }
}