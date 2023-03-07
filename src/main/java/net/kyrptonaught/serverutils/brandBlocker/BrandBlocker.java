package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
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

    @Override
    public BrandBlockerConfig createDefaultConfig() {
        return new BrandBlockerConfig();
    }
}