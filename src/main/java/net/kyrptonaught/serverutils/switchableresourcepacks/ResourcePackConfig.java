package net.kyrptonaught.serverutils.switchableresourcepacks;


import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfig extends AbstractConfigFile {

    Boolean autoRevoke = false;

    List<RPOption> packs = new ArrayList<>();

    public static class RPOption {
        enum PACKCOMPATIBILITY {
            VANILLA, OPTIFINE, BOTH
        }

        public String packname;

        public String url;

        public String hash;

        public boolean required = true;

        public boolean hasPrompt = true;

        public String message = "plz use me";

        public PACKCOMPATIBILITY packCompatibility = PACKCOMPATIBILITY.BOTH;
    }
}
