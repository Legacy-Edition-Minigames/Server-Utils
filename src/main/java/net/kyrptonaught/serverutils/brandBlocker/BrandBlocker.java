package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.VelocityProxyHelper;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BrandBlocker extends ModuleWConfig<BrandBlockerConfig> {

    public static Text isBlockedBrand(String brand) {
        BrandBlockerConfig config = ServerUtilsMod.BrandBlockerModule.getConfig();
        if (config.blockedBrands.containsKey(brand)) return getKickMsg(config.blockedBrands.get(brand), config.kickMsg);

        if (config.blockedBrandsRegex.size() == 0) return null;
        for (BrandBlockerConfig.BrandEntry brandEntry : config.blockedBrandsRegex)
            if (brandEntry.matchesRegex(brand)) return getKickMsg(brandEntry.kickMsg, config.kickMsg);

        return null;
    }

    private static Text getKickMsg(String msg, String kickMsg) {
        if (msg.equals("$KICKMSG$")) msg = kickMsg;
        try {
            return Text.Serializer.fromJson(msg);
        } catch (Exception ignored) {
            return Text.of(msg);
        }
    }

    public static void kickVelocity(ServerPlayerEntity player, ClientConnection connection, Text msg) {
        VelocityProxyHelper.kickVelocity(player, msg);
    }

    public static void kickMC(ServerPlayerEntity player, ClientConnection connection, Text msg) {
        connection.send(new DisconnectS2CPacket(msg));
        connection.disconnect(msg);
    }

    @Override
    public BrandBlockerConfig createDefaultConfig() {
        return new BrandBlockerConfig();
    }
}