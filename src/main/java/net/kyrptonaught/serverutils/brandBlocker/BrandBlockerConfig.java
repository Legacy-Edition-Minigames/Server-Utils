package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class BrandBlockerConfig implements AbstractConfigFile {

    private final HashMap<String, String> blockedBrands = new HashMap<>(Map.of("forge", "$KICKMSG$", "vanilla", "$KICKMSG$"));
    private final HashMap<String, String> blockedBrandsRegex = new HashMap<>();
    private final String kickMsg = "This client brand is not allowed";

    public Text isBlockedBrand(String brand) {
        if (blockedBrands.containsKey(brand)) return getKickMsg(blockedBrands.get(brand));

        if (blockedBrandsRegex.size() == 0) return null;

        for (String regex : blockedBrandsRegex.keySet())
            if (brand.matches(regex)) return getKickMsg(blockedBrandsRegex.get(regex));

        return null;
    }

    public Text getKickMsg(String msg) {
        if (msg.equals("$KICKMSG$")) msg = kickMsg;
        try {
            return Text.Serializer.fromJson(msg);
        } catch (Exception ignored) {
            return Text.of(msg);
        }
    }
}
