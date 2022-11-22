package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ScoreboardPlayerInfoNetworking {
    private static final Identifier HAS_MODS_PACKET = new Identifier(ServerUtilsMod.ScoreboardPlayerInfoModule.getMOD_ID(), "has_mods_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(HAS_MODS_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            Boolean hasLCH = getNextBool(packetByteBuf);
            Boolean hasOptishit = getNextBool(packetByteBuf);
            Boolean hasController = getNextBool(packetByteBuf);
            Integer guiScale = getNextInt(packetByteBuf);
            Integer panScale = getNextInt(packetByteBuf);
            server.execute(() -> {
                if (hasLCH != null) ScoreboardPlayerInfo.setHasLEMClient(player, hasLCH);
                if (hasOptishit != null) ScoreboardPlayerInfo.setHasOptifine(player, hasOptishit);
                if (hasController != null) ScoreboardPlayerInfo.setHasControllerMod(player, hasController);
                if (guiScale != null) ScoreboardPlayerInfo.setGUIScale(player, guiScale);
                if (panScale != null) ScoreboardPlayerInfo.setPanScale(player, panScale);
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
