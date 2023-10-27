package net.kyrptonaught.serverutils.mixin.scoreboardplayerinfo;

import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
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
    @Final
    private ClientConnection connection;

    @Inject(method = "onHandshake", at = @At("HEAD"))
    public void getProtocolVersion(HandshakeC2SPacket packet, CallbackInfo ci) {
        server.execute(() -> {
            if (packet.getIntendedState() == NetworkState.LOGIN) {
                int protocol = packet.getProtocolVersion();
                ScoreboardPlayerInfo.addClientConnectionProtocol(connection, protocol);
            }
        });
    }
}
