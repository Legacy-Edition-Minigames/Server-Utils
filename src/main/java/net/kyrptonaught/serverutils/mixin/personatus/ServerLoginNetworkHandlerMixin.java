package net.kyrptonaught.serverutils.mixin.personatus;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.kyrptonaught.serverutils.personatus.ServerLoginNetworkHandlerGetter;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URL;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin implements ServerLoginNetworkHandlerGetter {

    @Shadow
    GameProfile profile;

    @Final
    @Shadow
    MinecraftServer server;

    @Redirect(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;createPlayer(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/encryption/PlayerPublicKey;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    public ServerPlayerEntity SpoofProfile(PlayerManager instance, GameProfile profile, PlayerPublicKey publicKey) {
        if (PersonatusModule.isEnabled()) {
            try {
                if (server.getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                    String apiUrl = ServerUtilsMod.personatusModule.getConfig().getApiURL();
                    String lemResponse = sessionService.getAuthenticationService().performGetRequest(new URL(apiUrl + "/kvs/get/personatus/" + profile.getName()));
                    String responseName = ServerUtilsMod.config.getGSON().fromJson(lemResponse, JsonObject.class).get("value").getAsString();

                    MinecraftProfilePropertiesResponse profileResponse = ((YggdrasilInvoker) sessionService.getAuthenticationService()).invokeMakeRequest(new URL("https://api.mojang.com/users/profiles/minecraft/" + responseName), null, MinecraftProfilePropertiesResponse.class);
                    if (profileResponse != null) {
                        GameProfile spoofed = sessionService.fillProfileProperties(new GameProfile(profileResponse.getId(), responseName), true);
                        ((PersonatusProfile) profile).setRealProfile(spoofed);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (((PersonatusProfile) profile).isSpoofed()) {
                GameProfile spoofed = ((PersonatusProfile) profile).getRealProfile();
                ((PersonatusProfile) spoofed).setRealProfile(profile);
                profile = spoofed;
            }

        }
        return instance.createPlayer(profile, publicKey);
    }

    @Override
    public GameProfile getProfile() {
        return this.profile;
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }
}
