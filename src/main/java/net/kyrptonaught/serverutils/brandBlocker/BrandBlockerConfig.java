package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class BrandBlockerConfig extends AbstractConfigFile {

    public final HashMap<String, String> blockedBrands = new HashMap<>();

    public final List<BrandEntry> blockedBrandsRegex = new ArrayList<>();

    public final String kickMsg = "This client brand is not allowed";

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
