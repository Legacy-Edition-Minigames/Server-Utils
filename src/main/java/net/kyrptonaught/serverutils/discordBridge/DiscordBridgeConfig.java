package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class DiscordBridgeConfig extends AbstractConfigFile {

    public String BotToken;
    public String webhookURL;
    public long channelID;

    public String GameMessageName;
    public String GameMessageAvatarURL;

    public String PlayerSkinURL;

    public String PlayingStatus;

    public boolean isMenuBot;
    public long linkRoleID;

    public long loggingChannelID;
}
