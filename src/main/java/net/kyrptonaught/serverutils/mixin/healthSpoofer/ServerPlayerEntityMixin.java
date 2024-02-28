package net.kyrptonaught.serverutils.mixin.healthSpoofer;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);

        EntityAttributeInstance max_health = new EntityAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH, entityAttributeInstance -> {
        });
        max_health.setBaseValue(0);

        player.networkHandler.sendPacket(new EntityAttributesS2CPacket(getId(), Collections.singleton(max_health)));
    }
}
