package net.kyrptonaught.serverutils.mixin.welcomeMessage;

import net.kyrptonaught.serverutils.welcomeMessage.WelcomeModule;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onChatMessage", at = @At(value = "HEAD"))
    public void dontSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        WelcomeModule.trySendWelcomeMessage(this.server, this.player);
    }
}