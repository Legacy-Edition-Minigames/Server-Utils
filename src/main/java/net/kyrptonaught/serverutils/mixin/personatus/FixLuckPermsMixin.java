package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import me.lucko.luckperms.fabric.listeners.FabricConnectionListener;
import me.lucko.luckperms.fabric.mixin.ServerLoginNetworkHandlerAccessor;
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

    @Redirect(method = "onPreLogin", at = @At(value = "INVOKE", target = "Lme/lucko/luckperms/fabric/mixin/ServerLoginNetworkHandlerAccessor;getGameProfile()Lcom/mojang/authlib/GameProfile;"))
    public GameProfile spoofRealPermsPre(ServerLoginNetworkHandlerAccessor instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile();
    }


    @Redirect(method = "onLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;"))
    public UUID spoofRealPerms(ServerPlayerEntity instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }
}
