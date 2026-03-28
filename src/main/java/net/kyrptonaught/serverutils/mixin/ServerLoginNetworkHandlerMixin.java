package net.kyrptonaught.serverutils.mixin;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import one.oktw.mixin.core.ServerLoginNetworkHandlerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "one.oktw.PacketHandler")
public abstract class ServerLoginNetworkHandlerMixin {

    @Redirect(method = "lambda$handleVelocityPacket$0", at = @At(value = "INVOKE", target = "Lone/oktw/mixin/core/ServerLoginNetworkHandlerAccessor;setProfile(Lcom/mojang/authlib/GameProfile;)V"))
    public void earlylogin(ServerLoginNetworkHandlerAccessor login, GameProfile profile, PacketByteBuf buf, ServerLoginNetworkHandler handler) {
        login.setProfile(BackendServer.earlyLogin(handler, profile));
    }
}