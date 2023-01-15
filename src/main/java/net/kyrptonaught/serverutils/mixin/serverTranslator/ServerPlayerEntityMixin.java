package net.kyrptonaught.serverutils.mixin.serverTranslator;

import net.kyrptonaught.serverutils.serverTranslator.ServerTranslator;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "setClientSettings", at = @At("TAIL"))
    private void getClientLanguage(ClientSettingsC2SPacket packet, CallbackInfo ci) {
        ServerTranslator.registerPlayerLanguage((ServerPlayerEntity) (Object) this, packet.language());
    }
}
