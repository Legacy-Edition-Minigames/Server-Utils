package net.kyrptonaught.serverutils.backendLink.prohibitor;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class ProhibitorConfig extends AbstractConfigFile {
    public boolean globalChatEnabled = true;

    public boolean linkingEnabled = true;

    public WhitelistStatus whitelistStatus = WhitelistStatus.DISCORD;

}
