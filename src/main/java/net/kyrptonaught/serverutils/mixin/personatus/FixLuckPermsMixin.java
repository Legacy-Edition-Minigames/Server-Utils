package net.kyrptonaught.serverutils.mixin.personatus;

import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Pseudo
@Mixin(targets = "me.lucko.luckperms.fabric.listeners.FabricConnectionListener", remap = false)
public class FixLuckPermsMixin {

    @Dynamic("LuckPerms onLogin")
    @Redirect(method = "onLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getUuid()Ljava/util/UUID;"), remap = false)
    public UUID spoofRealPerms(ServerPlayerEntity instance) {
        return ((PersonatusProfile) instance.getGameProfile()).getRealProfile().getId();
    }
}
