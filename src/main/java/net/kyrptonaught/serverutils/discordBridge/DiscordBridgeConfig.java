package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class DiscordBridgeConfig extends AbstractConfigFile {

    public String BotToken;
    public String webhookURL;

    public String serverName;

    public String GameMessageName;
    public String GameMessageAvatarURL;

    public String PlayerSkinURL;

    public String PlayingStatus;

    public long bridgeChannelID;
    public long linkChannelID;
    public long loggingChannelID;

    public long linkRoleID;
}
