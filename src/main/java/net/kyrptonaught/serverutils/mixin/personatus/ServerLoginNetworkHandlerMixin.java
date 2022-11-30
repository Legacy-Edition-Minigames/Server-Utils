package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin{

    @Shadow
    GameProfile profile;

    @Final
    @Shadow
    MinecraftServer server;


    private boolean profileChecked = false;


    @Inject(method = "onHello", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;profile:Lcom/mojang/authlib/GameProfile;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void requestSpoof(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (!PersonatusModule.isEnabled()) {
            profileChecked = true;
            return;
        }

        new Thread(() -> {
            if (server.getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                try {
                    String responseName = PersonatusModule.URLGetValue(sessionService.getAuthenticationService(), ServerUtilsMod.personatusModule.getConfig().getApiURL() + "/kvs/get/personatus/" + profile.getName(), "value");
                    if (responseName != null) {
                        String responseUUID = PersonatusModule.URLGetValue(sessionService.getAuthenticationService(), "https://api.mojang.com/users/profiles/minecraft/" + responseName, "id");
                        if (responseUUID != null) {
                            UUID uuid = UUIDTypeAdapter.fromString(responseUUID);
                            GameProfile spoofed = sessionService.fillProfileProperties(new GameProfile(uuid, responseName), true);

                            //((PersonatusProfile) spoofed).setRealProfile(profile);
                            //profile = spoofed;
                             ((PersonatusProfile) profile).setRealProfile(spoofed);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            profileChecked = true;
        }).start();

    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;acceptPlayer()V", opcode = Opcodes.PUTFIELD), cancellable = true)
    public void delayAcceptUntilCheck(CallbackInfo ci) {
        if (!PersonatusModule.isEnabled()) return;
        if (!profileChecked)
            ci.cancel();
    }

    @Redirect(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;createPlayer(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/encryption/PlayerPublicKey;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    public ServerPlayerEntity SpoofProfile(PlayerManager instance, GameProfile profile, PlayerPublicKey publicKey) {
        if (PersonatusModule.isEnabled()) {
            if (((PersonatusProfile) profile).isSpoofed()) {
                GameProfile spoofed = ((PersonatusProfile) profile).getRealProfile();
                ((PersonatusProfile) spoofed).setRealProfile(profile);
                profile = spoofed;
            }
        }
        return instance.createPlayer(profile, publicKey);
    }
}
