package net.kyrptonaught.serverutils.personatus;

import io.netty.buffer.Unpooled;
import net.kyrptonaught.serverutils.mixin.personatus.PlayerListS2CPacketAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;

public class PacketCopier {

    public static PlayerListS2CPacket copyPlayerList(PlayerListS2CPacket oldListPacket) {
        PlayerListS2CPacket listPacket = new PlayerListS2CPacket(oldListPacket.getActions(), Collections.emptyList());
        ((PlayerListS2CPacketAccessor) listPacket).setEntries(new ArrayList<>(oldListPacket.getEntries()));
        return listPacket;
    }


    public static TeamS2CPacket copyTeamPacket(TeamS2CPacket oldTeamPacket) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        oldTeamPacket.write(buf);
        return new TeamS2CPacket(buf);
    }
}
