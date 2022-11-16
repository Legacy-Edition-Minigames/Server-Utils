package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
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

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin implements ServerLoginNetworkHandlerGetter {

    @Shadow
    GameProfile profile;

    @Final
    @Shadow
    MinecraftServer server;

    @Redirect(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;createPlayer(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/network/encryption/PlayerPublicKey;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    public ServerPlayerEntity SpoofProfile(PlayerManager instance, GameProfile profile, PlayerPublicKey publicKey) {
        if (((PersonatusProfile) profile).isSpoofed()) {
            GameProfile spoofed = ((PersonatusProfile) profile).getRealProfile();
            ((PersonatusProfile) spoofed).setRealProfile(profile);
            profile = spoofed;
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
