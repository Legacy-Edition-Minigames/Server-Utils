package net.kyrptonaught.serverutils.mixin.switchableresourcepacks;

import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ResourcePackStatusMixin extends ServerCommonNetworkHandler {

    @Shadow
    public ServerPlayerEntity player;

    public ResourcePackStatusMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        super.onResourcePackStatus(packet);
        switch (packet.status()) {
            case ACCEPTED ->
                    SwitchableResourcepacksMod.grantAdvancement(this.player, SwitchableResourcepacksMod.STARTED);
            case SUCCESSFULLY_LOADED ->
                    SwitchableResourcepacksMod.grantAdvancement(this.player, SwitchableResourcepacksMod.FINISHED);
            case FAILED_DOWNLOAD ->
                    SwitchableResourcepacksMod.grantAdvancement(this.player, SwitchableResourcepacksMod.FAILED);
        }
    }
}
