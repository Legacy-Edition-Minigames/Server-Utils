package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;createCommonPlayerSpawnInfo(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/network/packet/s2c/play/CommonPlayerSpawnInfo;"))
    public CommonPlayerSpawnInfo respawnSpoofDim(ServerPlayerEntity instance, ServerWorld world) {
        CommonPlayerSpawnInfo commonPlayerSpawnInfo = instance.createCommonPlayerSpawnInfo(world);
        return new CommonPlayerSpawnInfo(DimensionLoaderMod.tryGetDimKey(world), DimensionLoaderMod.tryGetWorldKey(world), commonPlayerSpawnInfo.seed(), commonPlayerSpawnInfo.gameMode(), commonPlayerSpawnInfo.prevGameMode(), commonPlayerSpawnInfo.isDebug(), commonPlayerSpawnInfo.isFlat(), commonPlayerSpawnInfo.lastDeathLocation(), commonPlayerSpawnInfo.portalCooldown());
    }


    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;createCommonPlayerSpawnInfo(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/network/packet/s2c/play/CommonPlayerSpawnInfo;"))
    public CommonPlayerSpawnInfo connectSpoofDim(ServerPlayerEntity instance, ServerWorld world) {
        CommonPlayerSpawnInfo commonPlayerSpawnInfo = instance.createCommonPlayerSpawnInfo(world);
        return new CommonPlayerSpawnInfo(DimensionLoaderMod.tryGetDimKey(world), DimensionLoaderMod.tryGetWorldKey(world), commonPlayerSpawnInfo.seed(), commonPlayerSpawnInfo.gameMode(), commonPlayerSpawnInfo.prevGameMode(), commonPlayerSpawnInfo.isDebug(), commonPlayerSpawnInfo.isFlat(), commonPlayerSpawnInfo.lastDeathLocation(), commonPlayerSpawnInfo.portalCooldown());
    }
}
