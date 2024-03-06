package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UndashedUuid;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginNetworkHandlerAccessor;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(targets = "net.minecraft.server.network.ServerLoginNetworkHandler$1")
public abstract class ServerLoginNetworkHandlerMixin {


    @Shadow
    @Final
    ServerLoginNetworkHandler field_14176;

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/ProfileResult;profile()Lcom/mojang/authlib/GameProfile;"))
    public GameProfile requestSpoof(ProfileResult instance) {
        if (PersonatusModule.isEnabled()) {
            GameProfile oldProfile = instance.profile();
            if (((ServerLoginNetworkHandlerAccessor) field_14176).getServer().getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                String responseName = PersonatusModule.URLGetValue(false, "kvs/get/personatus/" + oldProfile.getName(), "value");
                if (responseName != null) {
                    String responseUUID = PersonatusModule.URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + responseName, "id");
                    if (responseUUID != null) {
                        UUID uuid = UndashedUuid.fromString(responseUUID);
                        GameProfile spoofed = sessionService.fetchProfile(uuid, true).profile();

                        ((PersonatusProfile) spoofed).setRealProfile(oldProfile);
                        return spoofed;
                    }
                }
            }
        }

        return instance.profile();
    }
}