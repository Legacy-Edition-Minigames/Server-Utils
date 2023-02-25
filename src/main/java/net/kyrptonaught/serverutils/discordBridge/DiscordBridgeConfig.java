package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class DiscordBridgeConfig extends AbstractConfigFile {

    public String BotToken;
    public String webhookURL;
    public String channelID;

    public String GameMessageName;
    public String GameMessageAvatarURL;

    public String PlayerSkinURL;

    public String PlayingStatus;
}
