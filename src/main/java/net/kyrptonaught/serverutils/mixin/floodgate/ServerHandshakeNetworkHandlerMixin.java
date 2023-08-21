package net.kyrptonaught.serverutils.mixin.floodgate;

import net.kyrptonaught.serverutils.scoreboardPlayerInfo.QueuedPlayerData;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onHandshake", at= @At("HEAD"))
    public void checkForFloodgate(HandshakeC2SPacket packet, CallbackInfo ci){
        QueuedPlayerData playerDara = ScoreboardPlayerInfo.getQueuedPlayerData(this.connection, true);

        if(playerDara.isBedrock == null){
            playerDara.isBedrock = packet.getAddress().contains("^Floodgate^");
        }
    }
}
