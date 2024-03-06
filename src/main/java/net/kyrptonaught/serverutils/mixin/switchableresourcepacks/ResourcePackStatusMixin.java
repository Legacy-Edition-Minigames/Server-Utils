package net.kyrptonaught.serverutils.mixin.switchableresourcepacks;

import net.kyrptonaught.serverutils.switchableresourcepacks.PackStatus;
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
        System.out.println(this.player.getNameForScoreboard() + " " + packet.id() + " " + packet.status());
        switch (packet.status()) {
            case ACCEPTED ->
                    SwitchableResourcepacksMod.packStatusUpdate(this.player, packet.id(), PackStatus.LoadingStatus.STARTED);
            case SUCCESSFULLY_LOADED ->
                    SwitchableResourcepacksMod.packStatusUpdate(this.player, packet.id(), PackStatus.LoadingStatus.FINISHED);
            case DECLINED, FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD ->
                    SwitchableResourcepacksMod.packStatusUpdate(this.player, packet.id(), PackStatus.LoadingStatus.FAILED);
            case DISCARDED ->
                    SwitchableResourcepacksMod.packStatusUpdate(this.player, packet.id(), PackStatus.LoadingStatus.REMOVED);
        }
    }
}
