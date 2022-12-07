package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import me.lucko.luckperms.fabric.listeners.FabricConnectionListener;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Pseudo
@Mixin(FabricConnectionListener.class)
public class FixLuckPermsMixin {

    @Redirect(method = "onPreLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/dynamic/DynamicSerializableUuid;getUuidFromProfile(Lcom/mojang/authlib/GameProfile;)Ljava/util/UUID;"))
    public UUID spoofRealPermsPre(GameProfile profile) {
        return ((PersonatusProfile) profile).getRealProfile().getId();
    }

    @Redirect(method = "onLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;"))
    public UUID spoofRealPerms(ServerPlayerEntity instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }
}
