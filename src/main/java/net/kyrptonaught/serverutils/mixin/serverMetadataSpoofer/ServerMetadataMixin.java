package net.kyrptonaught.serverutils.mixin.serverMetadataSpoofer;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.serverMetadataSpoofer.ServerMetadataSpoofer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(MinecraftServer.class)
public class ServerMetadataMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerMetadata$Players;setSample([Lcom/mojang/authlib/GameProfile;)V"))
    public void spoofServerMetadata(ServerMetadata.Players instance, GameProfile[] sample) {
        instance.setSample(ServerMetadataSpoofer.spoofConnectedPlayers((MinecraftServer) (Object) this));
    }
}
