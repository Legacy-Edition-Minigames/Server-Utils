package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.mixin.networking.accessor.ServerLoginNetworkHandlerAccessor;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ScoreboardPlayerInfoNetworking {
    private static final Identifier HAS_MODS_PACKET = new Identifier(ServerUtilsMod.ScoreboardPlayerInfoModule.getMOD_ID(), "has_mods_packet");

    public static void registerReceivePacket() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            sender.sendPacket(HAS_MODS_PACKET, PacketByteBufs.create());
        });

        ServerLoginNetworking.registerGlobalReceiver(HAS_MODS_PACKET, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) {
                System.out.println("ScoreboardPlayerInfo received an error while querying the client for LCH");
                return;
            }
            Boolean hasLCH = getNextBool(buf);
            Boolean hasOptishit = getNextBool(buf);
            Boolean hasController = getNextBool(buf);
            Integer guiScale = getNextInt(buf);
            Integer panScale = getNextInt(buf);
            server.execute(() -> {
                QueuedPlayerData queuedPlayerData = ScoreboardPlayerInfo.getQueuedPlayerData(((ServerLoginNetworkHandlerAccessor) handler).getConnection(), true);
                queuedPlayerData.hasLCH = hasLCH;
                queuedPlayerData.hasOptishit = hasOptishit;
                queuedPlayerData.hasController = hasController;
                queuedPlayerData.guiScale = guiScale;
                queuedPlayerData.panScale = panScale;
            });
        });
    }

    public static Boolean getNextBool(PacketByteBuf packetByteBuf) {
        if (packetByteBuf.isReadable(1))
            return packetByteBuf.readBoolean();
        return null;
    }

    public static Integer getNextInt(PacketByteBuf packetByteBuf) {
        if (packetByteBuf.isReadable(4))
            return packetByteBuf.readInt();
        return null;
    }
}
