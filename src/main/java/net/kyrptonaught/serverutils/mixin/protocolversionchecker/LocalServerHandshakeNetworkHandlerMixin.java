package net.kyrptonaught.serverutils.mixin.protocolversionchecker;

import net.kyrptonaught.serverutils.protocolVersionChecker.ProtocolVersionChecker;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LocalServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalServerHandshakeNetworkHandler.class)
public abstract class LocalServerHandshakeNetworkHandlerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public abstract ClientConnection getConnection();

    @Inject(method = "onHandshake", at = @At("HEAD"))
    public void getProtocolVersion(HandshakeC2SPacket packet, CallbackInfo ci) {
        server.execute(() -> {
            if (packet.getIntendedState() == NetworkState.LOGIN) {
                int protocol = packet.getProtocolVersion();
                ProtocolVersionChecker.connectionProtocolVersion.put(getConnection(), protocol);
            }
        });
    }
}
