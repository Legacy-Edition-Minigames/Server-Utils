package net.kyrptonaught.serverutils.mixin.whitelistSync;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @ModifyArg(method = "kickNonWhitelistedPlayers", at = @At(target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V", value = "INVOKE"))
    public Text spoofWhitelistKickMSG(Text reason) {
        String spoof = ServerUtilsMod.whitelistSyncMod.getConfig().whitelistKickMSG;
        if (spoof != null && !spoof.isEmpty()) return Text.literal(spoof);

        return reason;
    }
}
