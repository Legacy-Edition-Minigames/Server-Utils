package net.kyrptonaught.serverutils.mixin.dropevent;

import net.kyrptonaught.serverutils.dropevent.DropEventMod;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
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

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"), cancellable = true)
    public void dropDis(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if (DropEventMod.ENABLED && (packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS)) {
            if (player.getMainHandStack().isEmpty()) {
                server.getCommandManager().execute(player.getCommandSource().withLevel(2).withSilent(), DropEventMod.getConfig().runCommand);
                ci.cancel();
            }
        }
    }
}
