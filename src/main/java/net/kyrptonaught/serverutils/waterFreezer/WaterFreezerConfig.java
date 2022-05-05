package net.kyrptonaught.serverutils.waterFreezer;

import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

public class WaterFreezerConfig implements AbstractConfigFile {

    @Comment("Set to true to stop fluids from flowing. See also /waterfreezer")
    public boolean FROZEN = false;
}
