package net.kyrptonaught.serverutils.dropevent;

import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

public class DropEventConfig implements AbstractConfigFile {

    @Comment("Command to be ran when the player attempts to drop with nothing in their hand")
    public String runCommand = "/say oopsie I dropped this";
}
