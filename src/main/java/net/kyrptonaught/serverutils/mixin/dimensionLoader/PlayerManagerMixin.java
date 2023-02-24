package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;
import java.util.Set;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDimensionKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<DimensionType> respawnSpoofDim(World instance) {
        return DimensionLoaderMod.tryGetDimKey((ServerWorld) instance);
    }

    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<World> respawnSpoofWorld(World instance) {
        return DimensionLoaderMod.tryGetWorldKey((ServerWorld) instance);
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getDimensionKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<DimensionType> connectSpoofDim(ServerWorld instance) {
        return DimensionLoaderMod.tryGetDimKey(instance);
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<World> connectSpoofWorld(ServerWorld instance) {
        return DimensionLoaderMod.tryGetWorldKey(instance);
    }
}
