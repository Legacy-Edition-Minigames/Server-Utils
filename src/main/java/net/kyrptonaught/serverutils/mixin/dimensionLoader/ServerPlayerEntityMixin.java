package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(method = "teleport", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket"))
    public PlayerRespawnS2CPacket tpSpoofDim(RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimension, long sha256Seed, GameMode gameMode, GameMode previousGameMode, boolean debugWorld, boolean flatWorld, boolean keepPlayerAttributes, Optional<GlobalPos> lastDeathPos, ServerWorld target) {
        return new PlayerRespawnS2CPacket(DimensionLoaderMod.tryGetDimKey(target), DimensionLoaderMod.tryGetWorldKey(target), sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, keepPlayerAttributes, lastDeathPos);

    }

    @Redirect(method = "moveToWorld", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket"))
    public PlayerRespawnS2CPacket mtwSpoofDim(RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimension, long sha256Seed, GameMode gameMode, GameMode previousGameMode, boolean debugWorld, boolean flatWorld, boolean keepPlayerAttributes, Optional<GlobalPos> lastDeathPos, ServerWorld target) {
        return new PlayerRespawnS2CPacket(DimensionLoaderMod.tryGetDimKey(target), DimensionLoaderMod.tryGetWorldKey(target), sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, keepPlayerAttributes, lastDeathPos);

    }
}
