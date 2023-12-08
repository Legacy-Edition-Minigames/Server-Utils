package net.kyrptonaught.serverutils.mixin.personatus;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
//@Mixin(FabricConnectionListener.class)
@Mixin(MinecraftServer.class)
public class FixLuckPermsMixin {

    /*
    @Redirect(method = "onPreLogin", at = @At(value = "INVOKE", target = "Lme/lucko/luckperms/fabric/mixin/ServerLoginNetworkHandlerAccessor;getGameProfile()Lcom/mojang/authlib/GameProfile;"))
    public GameProfile spoofRealPermsPre(ServerLoginNetworkHandlerAccessor instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile();
    }


    @Redirect(method = "onLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;"))
    public UUID spoofRealPerms(ServerPlayerEntity instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }

     */
}
