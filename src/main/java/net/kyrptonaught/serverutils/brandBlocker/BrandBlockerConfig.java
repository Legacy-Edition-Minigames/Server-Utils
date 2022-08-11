package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BrandBlockerConfig implements AbstractConfigFile {

    private final HashMap<String, String> blockedBrands = new HashMap<>(Map.of("forge", "$KICKMSG$", "vanilla", "$KICKMSG$"));
    public final List<BrandEntry> blockedBrandsRegex = new ArrayList<>();
    private final String kickMsg = "This client brand is not allowed";

    public Text isBlockedBrand(String brand) {
        if (blockedBrands.containsKey(brand)) return getKickMsg(blockedBrands.get(brand));

        if (blockedBrandsRegex.size() == 0) return null;
        for (BrandEntry brandEntry : blockedBrandsRegex)
            if (brandEntry.matchesRegex(brand)) return getKickMsg(brandEntry.kickMsg);

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

    public static class BrandEntry {
        public String regex;
        public String kickMsg = "$KICKMSG$";

        private transient Pattern regexPattern;

        public boolean matchesRegex(String input) {
            if (regexPattern == null) regexPattern = Pattern.compile(regex);
            return regexPattern.matcher(input).matches();
        }
    }
}
