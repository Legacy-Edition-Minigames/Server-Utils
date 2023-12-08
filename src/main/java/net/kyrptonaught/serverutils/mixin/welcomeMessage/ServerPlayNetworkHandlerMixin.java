package net.kyrptonaught.serverutils.mixin.welcomeMessage;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /*
    @Inject(method = "onChatMessage", at = @At(value = "HEAD"))
    public void dontSendMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        WelcomeModule.trySendWelcomeMessage(this.server, this.player);
    }

     */
}