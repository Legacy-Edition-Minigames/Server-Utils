package net.kyrptonaught.serverutils.mixin.personatus;

import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "createStatHandler", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getUuid()Ljava/util/UUID;"))
    public UUID getRealStats(PlayerEntity instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }

    @Redirect(method = "getAdvancementTracker", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;"))
    public UUID getRealAdvancements(ServerPlayerEntity instance) {
        System.out.println(((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId());
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }
}
