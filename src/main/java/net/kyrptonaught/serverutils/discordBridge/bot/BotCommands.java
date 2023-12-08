package net.kyrptonaught.serverutils.discordBridge.bot;

import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.MessageSender;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.kyrptonaught.serverutils.discordBridge.linking.LinkingManager;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.whitelistSync.WhitelistSyncMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

public class BotCommands {

    public static void registerCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("info", "Get the server info").setGuildOnly(true),
                Commands.slash("sus", "Mark a player as suspicous").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)).addOption(OptionType.STRING, "mcname", "MC Username").setGuildOnly(true),
                Commands.slash("unsus", "Mark a player as no longer suspicous").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS)).addOption(OptionType.STRING, "mcname", "MC Username").setGuildOnly(true),
                Commands.slash("syncedwhitelist", "Add/Remove a player from the whitelist").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER)).setGuildOnly(true)
                        .addSubcommands(
                                new SubcommandData("add", "Add to whitelist").addOption(OptionType.STRING, "mcname", "MC Username"),
                                new SubcommandData("remove", "Remove from whitelist").addOption(OptionType.STRING, "mcname", "MC Username"))
        ).queue();
    }

    public static void execute(BridgeBot bot, SlashCommandInteraction event) {
        switch (event.getName()) {
            case "info" -> infoCommandExecute(bot.server, event);
            case "sus" -> {
                event.deferReply().queue();
                String mcname = event.getOption("mcname").getAsString();
                susCommandExecute(mcname, (result) -> event.getHook().editOriginal(result).queue());
            }
            case "unsus" -> {
                event.deferReply().queue();
                String mcname = event.getOption("mcname").getAsString();
                unsusCommandExecute(mcname, (result) -> event.getHook().editOriginal(result).queue());
            }
            case "syncedwhitelist" -> {
                event.deferReply().queue();
                String mcname = event.getOption("mcname").getAsString();
                Optional<GameProfile> optional = bot.server.getUserCache().findByName(mcname);

                if (optional.isEmpty()) {
                    event.getHook().editOriginal(FormatToDiscord.toDiscord(bot.server, Text.translatable("argument.player.unknown"))).queue();
                    return;
                }
                ServerCommandSource source = getCommandSource(bot, event);
                if ("add".equals(event.getSubcommandName()))
                    WhitelistSyncMod.addWhitelist(source, Collections.singleton(optional.get()));
                else
                    WhitelistSyncMod.removeWhitelist(source, Collections.singleton(optional.get()));
            }
        }
    }

    public static void buttonPressed(BridgeBot bot, @NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId().equals("link:start")) {
            LinkingManager.displayLinkInput(event);
        }
    }

    public static void modalInteraction(BridgeBot bot, ModalInteractionEvent event) {
        if (event.getModalId().equals("link:modal")) {
            LinkingManager.linkInputResults(event);
        }
    }

    public static void infoCommandExecute(MinecraftServer server, SlashCommandInteraction event) {
        if (event.getChannel().getIdLong() != DiscordBridgeMod.config().bridgeChannelID) return;

        double serverTickTime = average(server.getTickTimes()) * 1.0E-6D;
        long freeRam = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        StringBuilder playerString = new StringBuilder();
        PlayerManager players = server.getPlayerManager();
        playerString.append("```css\n").append("Online Players (").append(players.getCurrentPlayerCount()).append("/").append(players.getMaxPlayerCount()).append(")\n");
        for (ServerPlayerEntity player : players.getPlayerList())
            playerString.append("[").append(player.networkHandler.getLatency()).append("ms] ").append(player.getNameForScoreboard()).append("\n");
        playerString.append("```");

        event.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Server Status")
                        .setDescription(playerString.toString())
                        .setColor(0x00aaff)
                        .addField("TPS", String.format("%.2f", Math.min(1000.0 / serverTickTime, 20)), true)
                        .addField("MSPT", String.format("%.2f", serverTickTime), true)
                        .addField("RAM", freeRam + "MB / " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB", true)
                        .build()).queue();
    }


    public static void susCommandExecute(String mcName, Consumer<String> result) {
        String responseUUID = PersonatusModule.URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + mcName, "id");

        if (responseUUID == null) {
            result.accept("Invalid MC Name");
            return;
        }

        BackendServerModule.asyncPost("link/sus/add/" + responseUUID, (success, response) -> {
            if (success) {
                result.accept("Added suspicous player" + mcName);
                MessageSender.sendLogMessage("Added " + mcName + " as a suspicous player");
            } else
                result.accept("Error");
        });
    }

    public static void unsusCommandExecute(String mcName, Consumer<String> result) {
        String responseUUID = PersonatusModule.URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + mcName, "id");

        if (responseUUID == null) {
            result.accept("Invalid MC Name");
            return;
        }

        BackendServerModule.asyncPost("link/sus/remove/" + responseUUID, (success, response) -> {
            if (success) {
                result.accept("Removed suspicous player" + mcName);
                MessageSender.sendLogMessage("Removed " + mcName + " as a suspicous player");
            } else
                result.accept("Error");
        });
    }

    private static double average(long[] array) {
        long l = 0L;
        for (long m : array) {
            l += m;
        }
        return (double) l / (double) array.length;
    }


    private static ServerCommandSource getCommandSource(BridgeBot bot, SlashCommandInteraction event) {
        return new ServerCommandSource(new DiscordCommandOutput(bot.server, event.getHook()), Vec3d.ZERO, Vec2f.ZERO, bot.server.getOverworld(), 4, "Discord/" + event.getMember().getEffectiveName(), Text.literal("Discord/" + event.getMember().getEffectiveName()), bot.server, null);
    }
}
