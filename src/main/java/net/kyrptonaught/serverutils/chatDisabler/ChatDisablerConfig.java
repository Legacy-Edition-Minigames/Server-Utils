package net.kyrptonaught.serverutils.chatDisabler;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class ChatDisablerConfig extends AbstractConfigFile {

    public boolean notifyChatDisabled = true;

    public String disabledMessage = "Chat has temporarily been disabled. Other players will not see your messages";

    public boolean notifyChatEnabled = true;

    public String enabledMessage = "Chat has been re-enabled. Other players can now receive your messages";

    public boolean informClientMSGNotSent = false;

    public String disabledResponse = "Chat is temporarily disabled, your message was not sent.";
}
