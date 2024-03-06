package net.kyrptonaught.serverutils.switchableresourcepacks;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfig extends AbstractConfigFile {
    List<RPOption> packs = new ArrayList<>();

    public static class RPOption {
        public String packname;

        public String url;

        public String hash;

        public boolean required = true;

        public boolean hasPrompt = true;

        public String message = "plz use me";
    }
}
