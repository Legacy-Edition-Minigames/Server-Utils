package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value = "NEW", target = "(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/RegistryKey;JLnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;ZZBLjava/util/Optional;I)Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;"))
    public PlayerRespawnS2CPacket tpSpoofDim(RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimension, long sha256Seed, GameMode gameMode, GameMode previousGameMode, boolean debugWorld, boolean flatWorld, byte flag, Optional<GlobalPos> lastDeathPos, int portalcooldown, ServerWorld targetWorld) {
        return new PlayerRespawnS2CPacket(DimensionLoaderMod.tryGetDimKey(targetWorld), DimensionLoaderMod.tryGetWorldKey(targetWorld), sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, (byte) 3, lastDeathPos, portalcooldown);

    }

    @Redirect(method = "moveToWorld", at = @At(value = "NEW", target = "(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/RegistryKey;JLnet/minecraft/world/GameMode;Lnet/minecraft/world/GameMode;ZZBLjava/util/Optional;I)Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;"))
    public PlayerRespawnS2CPacket mtwSpoofDim(RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimension, long sha256Seed, GameMode gameMode, GameMode previousGameMode, boolean debugWorld, boolean flatWorld, byte flag, Optional<GlobalPos> lastDeathPos, int portalcooldown, ServerWorld targetWorld) {
        return new PlayerRespawnS2CPacket(DimensionLoaderMod.tryGetDimKey(targetWorld), DimensionLoaderMod.tryGetWorldKey(targetWorld), sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, (byte) 3, lastDeathPos, portalcooldown);

    }
}
