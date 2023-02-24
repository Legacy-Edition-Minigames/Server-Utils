package net.kyrptonaught.serverutils.mixin.dimensionLoader;

import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Redirect(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getDimensionKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<DimensionType> TPSpoofDim(ServerWorld instance) {
        return DimensionLoaderMod.tryGetDimKey(instance);
    }

    @Redirect(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<World> TPSpoofWorld(ServerWorld instance) {
        return DimensionLoaderMod.tryGetWorldKey(instance);
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getDimensionKey()Lnet/minecraft/util/registry/RegistryKey;"))
    public RegistryKey<DimensionType> MTWSpoofDim(ServerWorld instance) {
        return DimensionLoaderMod.tryGetDimKey(instance);
    }

    @Redirect(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRegistryKey()Lnet/minecraft/util/registry/RegistryKey;"),
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getLevelProperties()Lnet/minecraft/world/WorldProperties;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getPlayerManager()Lnet/minecraft/server/PlayerManager;")))
    public RegistryKey<World> MTWSpoofWorld(ServerWorld instance) {
        return DimensionLoaderMod.tryGetWorldKey(instance);
    }
}
