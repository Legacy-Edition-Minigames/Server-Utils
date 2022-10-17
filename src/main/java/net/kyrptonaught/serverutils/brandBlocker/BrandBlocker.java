package net.kyrptonaught.serverutils.brandBlocker;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ByteBufDataOutput;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.velocityserverswitch.VelocityServerSwitchMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BrandBlocker {
    public static String MOD_ID = "brandblocker";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new BrandBlockerConfig());
    }

    public static BrandBlockerConfig getConfig() {
        return (BrandBlockerConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }

    public static Text isBlockedBrand(String brand) {
        BrandBlockerConfig config = getConfig();
        if (config.blockedBrands.containsKey(brand)) return getKickMsg(config.blockedBrands.get(brand));

        if (config.blockedBrandsRegex.size() == 0) return null;
        for (BrandBlockerConfig.BrandEntry brandEntry : config.blockedBrandsRegex)
            if (brandEntry.matchesRegex(brand)) return getKickMsg(brandEntry.kickMsg);

        return null;
    }

    private static Text getKickMsg(String msg) {
        if (msg.equals("$KICKMSG$")) msg = getConfig().kickMsg;
        try {
            return Text.Serializer.fromJson(msg);
        } catch (Exception ignored) {
            return Text.of(msg);
        }
    }

    public static void kickVelocity(ServerPlayerEntity player, ClientConnection connection, Text msg) {
        try (ByteBufDataOutput output = new ByteBufDataOutput(new PacketByteBuf(Unpooled.buffer()))) {
            output.writeUTF("KickPlayer");
            output.writeUTF(player.getEntityName());
            output.writeUTF(msg.getString());

            ServerPlayNetworking.send(player, VelocityServerSwitchMod.BUNGEECORD_ID, output.getBuf());
        } catch (Exception e) {
            System.out.println("Failed to send kick packet");
            e.printStackTrace();
        }
    }

    public static void kickMC(ServerPlayerEntity player, ClientConnection connection, Text msg) {
        connection.send(new DisconnectS2CPacket(msg));
        connection.disconnect(msg);
    }
}