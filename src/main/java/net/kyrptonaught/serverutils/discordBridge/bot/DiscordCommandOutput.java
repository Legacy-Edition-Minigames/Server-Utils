package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.interactions.InteractionHook;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;


public class DiscordCommandOutput implements CommandOutput {
    private final MinecraftServer server;
    private final InteractionHook hook;

    public DiscordCommandOutput(MinecraftServer server, InteractionHook hook) {
        this.server = server;
        this.hook = hook;
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldTrackOutput() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public void sendMessage(Text message) {
        hook.editOriginal(FormatToDiscord.toDiscord(server, message)).queue();
    }
}
