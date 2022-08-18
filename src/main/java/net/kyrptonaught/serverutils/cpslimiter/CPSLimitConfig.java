package net.kyrptonaught.serverutils.cpslimiter;

import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

public class CPSLimitConfig implements AbstractConfigFile {

    @Comment("Smoothing bias toward current avg -> prev avg")
    public double smoothing = 0.7f;

    public double CPSLimit = 14;
}
