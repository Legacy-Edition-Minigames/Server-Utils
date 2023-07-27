package net.kyrptonaught.serverutils.mixin.playerJoinLocation;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.playerJoinLocation.PlayerJoinLocationConfig;
import net.kyrptonaught.serverutils.playerJoinLocation.PlayerJoinLocationMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "onPlayerConnect", at = @At(target = "Lnet/minecraft/world/dimension/DimensionType;worldFromDimensionNbt(Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/DataResult;", value = "INVOKE"))
    public DataResult<RegistryKey<World>> forceSpawnLocation(Dynamic<?> nbt, ClientConnection connection, ServerPlayerEntity player) {
        if (PlayerJoinLocationMod.ENABLED) {
            PlayerJoinLocationConfig config = ServerUtilsMod.playerJoinLocationMod.getConfig();
            player.setPos(config.posX, config.posY, config.posZ);
            return DataResult.success(World.OVERWORLD);
        }
        return DimensionType.worldFromDimensionNbt(nbt);
    }
}
