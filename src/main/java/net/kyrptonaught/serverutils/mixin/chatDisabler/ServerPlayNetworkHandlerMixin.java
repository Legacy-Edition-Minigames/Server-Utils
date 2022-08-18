package net.kyrptonaught.serverutils.mixin.chatDisabler;

import net.kyrptonaught.serverutils.chatDisabler.ChatDisabler;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;
import java.util.function.Function;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Redirect(method = "handleMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public void dontSendMessage(PlayerManager instance, Text serverMessage, Function<ServerPlayerEntity, Text> playerMessageFactory, MessageType type, UUID sender) {
        if (ChatDisabler.CHATENABLED)
            instance.broadcast(serverMessage, playerMessageFactory, type, sender);
        else if (ChatDisabler.getConfig().informClientMSGNotSent)
            this.player.sendMessage(new LiteralText(ChatDisabler.getConfig().disabledResponse), MessageType.SYSTEM, Util.NIL_UUID);
    }
}