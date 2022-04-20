package net.kyrptonaught.serverutils.switchableresourcepacks;

import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfig implements AbstractConfigFile {

    @Comment("Should all criteria be automatically revoked next time the command is executed.")
    Boolean autoRevoke = false;

    @Comment("List of all the resourcepacks configurations to use")
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
