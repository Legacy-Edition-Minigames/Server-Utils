package net.kyrptonaught.serverutils.chatDisabler;

import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

public class ChatDisablerConfig implements AbstractConfigFile {

    @Comment("Should players be notified when chat is disabled")
    public boolean notifyChatDisabled = true;

    @Comment("Message to be sent to players when chat gets disabled")
    public String disabledMessage = "Chat has temporarily been disabled. Other players will not see your messages";

    @Comment("Should players be notified when chat is enabled")
    public boolean notifyChatEnabled = true;

    @Comment("Message to be sent to players when chat gets enabled")
    public String enabledMessage = "Chat has been re-enabled. Other players can now receive your messages";

    @Comment("Should A response be sent if a player tries to send a message when chat is disabled")
    public boolean informClientMSGNotSent = false;

    @Comment("Response to player who attempts to chat when chat is disabled")
    public String disabledResponse = "Chat is temporarily disabled, your message was not sent.";
}
