package net.kyrptonaught.serverutils.mixin.personatus;


import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.kyrptonaught.serverutils.personatus.ServerLoginNetworkHandlerGetter;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URL;

@Mixin(targets = "net.minecraft.server.network.ServerLoginNetworkHandler$1")
public abstract class ServerLoginNetworkHandler1Mixin {

    @Shadow
    @Final
    ServerLoginNetworkHandler field_14176;

    @Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;state:Lnet/minecraft/server/network/ServerLoginNetworkHandler$State;"))
    public void SpoofProfile(CallbackInfo ci) {
        ServerLoginNetworkHandlerGetter handler = (ServerLoginNetworkHandlerGetter) this.field_14176;
        GameProfile gameProfile = handler.getProfile();
        try {
            if (handler.getServer().getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                String apiUrl = ServerUtilsMod.AdvancementSyncModule.getConfig().getApiURL();
                String lemResponse = sessionService.getAuthenticationService().performGetRequest(new URL(apiUrl + "/kvs/get/personatus/" + gameProfile.getName()));
                String responseName = ServerUtilsMod.config.getGSON().fromJson(lemResponse, JsonObject.class).get("value").getAsString();

                MinecraftProfilePropertiesResponse profileResponse = ((YggdrasilInvoker) sessionService.getAuthenticationService()).invokeMakeRequest(new URL("https://api.mojang.com/users/profiles/minecraft/" + responseName), null, MinecraftProfilePropertiesResponse.class);
                if (profileResponse != null) {
                    GameProfile spoofed = sessionService.fillProfileProperties(new GameProfile(profileResponse.getId(), responseName), true);
                    ((PersonatusProfile) gameProfile).setRealProfile(spoofed);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
