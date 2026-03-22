package net.kyrptonaught.serverutils.mixin.prohibitor;

import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private static void honorMute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) {
        if (source.isExecutedByPlayer() && ProhibitorModule.canChat(source.getPlayer())) {
            source.getPlayer().sendMessage(Text.translatable("prohibitor.mute.cannotsent"), false);
            ci.cancel();
        }
    }
}
