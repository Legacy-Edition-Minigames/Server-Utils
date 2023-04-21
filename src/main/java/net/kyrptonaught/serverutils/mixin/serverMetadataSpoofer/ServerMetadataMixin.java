package net.kyrptonaught.serverutils.mixin.serverMetadataSpoofer;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.serverMetadataSpoofer.ServerMetadataSpoofer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;


@Mixin(MinecraftServer.class)
public abstract class ServerMetadataMixin {

    @Shadow
    public abstract int getMaxPlayerCount();

    @Redirect(method = "createMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadataPlayers()Lnet/minecraft/server/ServerMetadata$Players;"))
    public ServerMetadata.Players spoofServerMetadata(MinecraftServer instance) {
        List<GameProfile> players = ServerMetadataSpoofer.spoofConnectedPlayers((MinecraftServer) (Object) this);
        return new ServerMetadata.Players(this.getMaxPlayerCount(), players.size(), players);
    }
}
