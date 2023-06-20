package net.kyrptonaught.serverutils;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VelocityProxyHelper {
    private static final Identifier BUNGEECORD_ID = new Identifier("bungeecord", "main");

    public static void getPlayerCount(ServerPlayerEntity player) {
        sendVelocityCommand(player, "PlayerList", "ALL");
    }

    public static void switchServer(ServerPlayerEntity player, String servername) {
        sendVelocityCommand(player, "Connect", servername);
    }

    public static void kickPlayer(ServerPlayerEntity player, Text msg) {
        sendVelocityCommand(player, "KickPlayer", ((PersonatusProfile) player.getGameProfile()).getRealProfile().getName(), msg.getString());
        player.networkHandler.sendPacket(new DisconnectS2CPacket(msg));
        player.networkHandler.disconnect(msg);
    }

    public static void kickPlayer(ServerPlayerEntity player, String msg) {
        kickPlayer(player, Text.literal(msg));
    }

    private static void sendVelocityCommand(ServerPlayerEntity player, String command, String... args) {
        try (VelocityPacket.ByteBufDataOutput output = new VelocityPacket.ByteBufDataOutput(new PacketByteBuf(Unpooled.buffer()))) {
            output.writeUTF(command);
            for (String arg : args)
                output.writeUTF(arg);

            ServerPlayNetworking.send(player, BUNGEECORD_ID, (PacketByteBuf) output.getBuf());
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