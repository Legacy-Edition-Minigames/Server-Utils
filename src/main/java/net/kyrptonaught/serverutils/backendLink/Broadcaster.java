package net.kyrptonaught.serverutils.backendLink;


import net.kyrptonaught.serverutils.backendLink.discordBridge.DiscordBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class Broadcaster {

    public static void GameStart(MinecraftServer server) {
        broadcast(server, Text.translatable("lem.game.start.start"));
    }

    public static void LoadPreset(MinecraftServer server) {
        broadcast2(server, Text.translatable("lem.menu.host.config.update.generic", Text.translatable("lem.menu.host.config.preset.load")));
    }

    public static void Showdown(MinecraftServer server) {
        broadcast(server, Text.translatable("lem.game.showdown"));
    }

    public static void Won(MinecraftServer server, Text player) {
        broadcast(server, Text.translatable("lem.game.win", player));
    }

    public static void Draw(MinecraftServer server) {
        broadcast(server, Text.translatable("lem.game.draw"));
    }

    public static void Host(MinecraftServer server, Text player) {
        broadcast2(server, Text.empty().append(player).append(" has become the host!"));
    }

    public static void egg(MinecraftServer server, Text egg) {
        server.getPlayerManager().broadcast(egg, false);
        DiscordBridge.sendMessage(egg, 0xe3dd2b);
    }

    public static void broadcast(MinecraftServer server, Text message) {
        DiscordBridge.sendMessage(message, 0xffffff);
    }

    public static void broadcast2(MinecraftServer server, Text message) {
        server.getPlayerManager().broadcast(message, false);
        DiscordBridge.sendMessage(message, 0xffffff);
    }
}
