package net.kyrptonaught.serverutils.mixin;

import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.network.ServerLoginNetworkHandler$1")
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    @Final
    private ServerLoginNetworkHandler field_14176;

    /*
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/ProfileResult;profile()Lcom/mojang/authlib/GameProfile;"))
    public GameProfile earlyLogin(ProfileResult instance) {
        return BackendServerModule.earlyLogin(field_14176, instance.profile());
    }

     */
}