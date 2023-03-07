package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {

    @Shadow
    GameProfile profile;

    @Final
    @Shadow
    MinecraftServer server;

    private boolean profileChecked = false;


    @Inject(method = "onHello", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;profile:Lcom/mojang/authlib/GameProfile;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void requestSpoof(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (!PersonatusModule.isEnabled()) {
            profileChecked = true;
            return;
        }

        new Thread(() -> {
            if (server.getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                String responseName = PersonatusModule.URLGetValue(false, "kvs/get/personatus/" + profile.getName(), "value");
                if (responseName != null) {
                    String responseUUID = PersonatusModule.URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + responseName, "id");
                    if (responseUUID != null) {
                        UUID uuid = UUIDTypeAdapter.fromString(responseUUID);
                        GameProfile spoofed = sessionService.fillProfileProperties(new GameProfile(uuid, responseName), true);

                        ((PersonatusProfile) spoofed).setRealProfile(profile);
                        profile = spoofed;
                    }
                }
            }
            profileChecked = true;
        }).start();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginNetworkHandler;acceptPlayer()V"), cancellable = true)
    public void delayAcceptUntilCheck(CallbackInfo ci) {
        if (!PersonatusModule.isEnabled()) return;
        if (!profileChecked)
            ci.cancel();
    }

    @Inject(method = "getVerifiedPublicKey", at = @At("HEAD"), cancellable = true)
    private static void noKey4u(PlayerPublicKey.PublicKeyData publicKeyData, UUID playerUuid, SignatureVerifier servicesSignatureVerifier, boolean shouldThrowOnMissingKey, CallbackInfoReturnable<PlayerPublicKey> cir) {
        cir.setReturnValue(null);
    }

    @Dynamic("Fabric Proxy Mixin")
    @Inject(method = "setRealUUID", at = @At("HEAD"), cancellable = true, require = 0)
    public void stopThatFabricProxyItsverysilly(UUID uuid, CallbackInfo ci) {
        if (PersonatusModule.isEnabled())
            ci.cancel();
    }

    @Dynamic("Fabric Proxy Mixin")
    @Inject(method = "setProfile", at = @At("HEAD"), cancellable = true, require = 0)
    public void stopThatFabricProxyItsverysilly2(GameProfile profile, CallbackInfo ci) {
        if (PersonatusModule.isEnabled())
            ci.cancel();
    }
}
