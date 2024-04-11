package net.kyrptonaught.serverutils;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VelocityProxyHelper {
    private static final Identifier BUNGEECORD_ID = new Identifier("bungeecord", "main");

    public static void getPlayerCount(ServerPlayerEntity player) {
        //sendVelocityCommand(player, "PlayerList", "ALL");
    }

    public static void switchServer(ServerPlayerEntity player, String servername) {
        sendVelocityCommand(player.networkHandler, "Connect", servername);
    }

    public static void kickPlayer(ServerCommonNetworkHandler handler, GameProfile profile, Text msg) {
        sendVelocityCommand(handler, "KickPlayer", ((PersonatusProfile) profile).getRealProfile().getName(), msg.getString());
        handler.sendPacket(new DisconnectS2CPacket(msg));
        handler.disconnect(msg);
    }

    private static void sendVelocityCommand(ServerCommonNetworkHandler handler, String command, String... args) {
        try (VelocityPacket.ByteBufDataOutput output = new VelocityPacket.ByteBufDataOutput(new PacketByteBuf(Unpooled.buffer()))) {
            output.writeUTF(command);
            for (String arg : args)
                output.writeUTF(arg);

            handler.sendPacket(ServerPlayNetworking.createS2CPacket(BUNGEECORD_ID, (PacketByteBuf) output.getBuf()));
        } catch (Exception e) {
            System.out.println("Failed to send command to Velocity Proxy: ");
            e.printStackTrace();
        }
    }

    public static void registerReceive() {
        /*
        ServerPlayNetworking.registerGlobalReceiver(BUNGEECORD_ID, (server, player, handler, buf, responseSender) -> {
            VelocityPacket.ByteBufDataInput input = new VelocityPacket.ByteBufDataInput(buf);

            String test = input.readUTF();
            String test2 = input.readUTF();
            String test3 = input.readUTF();

            System.out.println(buf);
        });
         */
    }
}