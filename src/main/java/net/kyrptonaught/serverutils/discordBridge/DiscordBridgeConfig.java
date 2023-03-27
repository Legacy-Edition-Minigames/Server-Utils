package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class DiscordBridgeConfig extends AbstractConfigFile {

    public String BotToken;
    public String webhookURL;

    public String serverName;

    public String PlayerSkinURL;

    public String PlayingStatus;

    public long bridgeChannelID;
    public long linkChannelID;
    public String loggingWebhookURL;

    public long linkRoleID;
    public long moderatorRoleID;

    public boolean canDiscordMSGCMDPing = true;
}
