package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.Set;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Redirect(method = "respawnPlayer", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket"))
    public PlayerRespawnS2CPacket mtwSpoofDim(RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimension, long sha256Seed, GameMode gameMode, GameMode previousGameMode, boolean debugWorld, boolean flatWorld, byte flag, Optional<GlobalPos> lastDeathPos, ServerPlayerEntity player, boolean alive) {
        BlockPos blockPos = player.getSpawnPointPosition();
        ServerWorld target = this.server.getWorld(player.getSpawnPointDimension());
        Optional<Vec3d> optional = target != null && blockPos != null ? PlayerEntity.findRespawnPosition(target, blockPos, player.getSpawnAngle(), player.isSpawnForced(), alive) : Optional.empty();
        target = target != null && optional.isPresent() ? target : this.server.getOverworld();

        return new PlayerRespawnS2CPacket(DimensionLoaderMod.tryGetDimKey(target), DimensionLoaderMod.tryGetWorldKey(target), sha256Seed, gameMode, previousGameMode, debugWorld, flatWorld, alive ? (byte)1 : 0, lastDeathPos);
    }


    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/network/packet/s2c/play/GameJoinS2CPacket"))
    public GameJoinS2CPacket mtwSpoofDim(int playerEntityId, boolean hardcore, GameMode gameMode, @Nullable GameMode previousGameMode, Set<RegistryKey<World>> dimensionIds, DynamicRegistryManager.Immutable registryManager, RegistryKey<DimensionType> dimensionType, RegistryKey<World> dimensionId, long sha256Seed, int maxPlayers, int viewDistance, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean debugWorld, boolean flatWorld, Optional<GlobalPos> lastDeathLocation, ClientConnection connection, ServerPlayerEntity player) {
        ServerWorld target = player.getWorld();
        return new GameJoinS2CPacket(playerEntityId, hardcore, gameMode, previousGameMode, dimensionIds, registryManager, DimensionLoaderMod.tryGetDimKey(target), DimensionLoaderMod.tryGetWorldKey(target), sha256Seed, maxPlayers, viewDistance, simulationDistance, reducedDebugInfo, showDeathScreen, debugWorld, flatWorld, lastDeathLocation);
    }
}
