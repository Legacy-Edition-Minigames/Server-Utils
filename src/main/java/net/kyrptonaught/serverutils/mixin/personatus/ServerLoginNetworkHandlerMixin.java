package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UndashedUuid;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import one.oktw.mixin.core.ServerLoginNetworkHandlerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(targets = "one.oktw.PacketHandler")
public abstract class ServerLoginNetworkHandlerMixin {


    @Redirect(method = "lambda$handleVelocityPacket$0", at = @At(value = "INVOKE", target = "Lone/oktw/mixin/core/ServerLoginNetworkHandlerAccessor;setProfile(Lcom/mojang/authlib/GameProfile;)V"))
    public void requestSpoof(ServerLoginNetworkHandlerAccessor login, GameProfile profile, PacketByteBuf buf, ServerLoginNetworkHandler handler) {
        if (PersonatusModule.isEnabled()) {
            MinecraftServer server = ((net.fabricmc.fabric.mixin.networking.accessor.ServerLoginNetworkHandlerAccessor) handler).getServer();
            if (server.getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                String responseName = PersonatusModule.URLGetValue(false, "kvs/get/personatus/" + profile.getName(), "value");
                if (responseName != null) {
                    String responseUUID = PersonatusModule.URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + responseName, "id");
                    if (responseUUID != null) {
                        UUID uuid = UndashedUuid.fromString(responseUUID);
                        GameProfile spoofed = sessionService.fetchProfile(uuid, true).profile();

                        ((PersonatusProfile) spoofed).setRealProfile(profile);
                        login.setProfile(spoofed);
                        return;
                    }
                }
            }
        }

        login.setProfile(profile);
    }
}