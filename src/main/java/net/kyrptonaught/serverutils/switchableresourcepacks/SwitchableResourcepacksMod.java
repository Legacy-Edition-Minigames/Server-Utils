package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SwitchableResourcepacksMod extends ModuleWConfig<ResourcePackConfig> {
    public static final HashMap<String, ResourcePackConfig.RPOption> rpOptionHashMap = new HashMap<>();

    public static final HashMap<UUID, PackStatus> playerLoaded = new HashMap<>();

    public void onConfigLoad(ResourcePackConfig config) {
        rpOptionHashMap.clear();
        config.packs.forEach(rpOption -> rpOptionHashMap.put(rpOption.packname, rpOption));

        if (config.packs.isEmpty()) {
            ResourcePackConfig.RPOption option = new ResourcePackConfig.RPOption();
            option.packname = "example_pack";
            option.url = "https://example.com/resourcepack.zip";
            option.hash = "examplehash";
            config.packs.add(option);
            saveConfig();
            System.out.println("[" + getMOD_ID() + "]: Generated example resourcepack config");
        }
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            playerLoaded.remove(handler.getPlayer().getUuid());
        });
    }

    @Override
    public ResourcePackConfig createDefaultConfig() {
        return new ResourcePackConfig();
    }

    public static void packStatusUpdate(ServerPlayerEntity player, UUID packname, PackStatus.LoadingStatus status) {
        if (!playerLoaded.containsKey(player.getUuid()))
            playerLoaded.put(player.getUuid(), new PackStatus());

        playerLoaded.get(player.getUuid()).setPackLoadStatus(packname, status);
    }

    public static void addPackStatus(ServerPlayerEntity player, UUID packname, boolean temp) {
        if (!playerLoaded.containsKey(player.getUuid()))
            playerLoaded.put(player.getUuid(), new PackStatus());

        playerLoaded.get(player.getUuid()).addPack(packname, temp);
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("loadresource").requires((source) -> source.hasPermissionLevel(0));
        for (String packname : rpOptionHashMap.keySet()) {
            cmd.then(CommandManager.literal(packname)
                    .then(CommandManager.argument("player", EntityArgumentType.players())
                            .requires((source) -> source.hasPermissionLevel(2))
                            .executes(commandContext -> execute(commandContext, packname, EntityArgumentType.getPlayers(commandContext, "player"))))
                    .executes(commandContext -> execute(commandContext, packname, Collections.singleton(commandContext.getSource().getPlayer()))));
        }
        dispatcher.register(cmd);
    }

    public int execute(CommandContext<ServerCommandSource> commandContext, String packname, Collection<ServerPlayerEntity> players) {
        if (execute(packname, players)) {
            commandContext.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Enabled pack: " + packname), false);
        } else {
            commandContext.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Packname: " + packname + " was not found"), false);
        }
        return 1;
    }

    private static boolean execute(String packname, Collection<ServerPlayerEntity> players) {
        ResourcePackConfig.RPOption rpOption = rpOptionHashMap.get(packname);
        if (rpOption == null) {
            return false;
        }

        UUID packUUID = UUID.nameUUIDFromBytes(rpOption.packname.getBytes(StandardCharsets.UTF_8));
        for (ServerPlayerEntity player : players) {
            if (playerLoaded.containsKey(player.getUuid()) && playerLoaded.get(player.getUuid()).getPacks().containsKey(packUUID))
                continue;

            addPackStatus(player, packUUID, false);
            player.networkHandler.sendPacket(new ResourcePackSendS2CPacket(packUUID, rpOption.url, rpOption.hash, rpOption.required, rpOption.hasPrompt ? Text.literal(rpOption.message) : null));
        }
        return true;
    }

    public static void addPacks(List<ResourcePackConfig.RPOption> packList, ServerPlayerEntity player) {
        for (ResourcePackConfig.RPOption rpOption : packList) {
            UUID packUUID = UUID.nameUUIDFromBytes(rpOption.packname.getBytes(StandardCharsets.UTF_8));
            addPackStatus(player, packUUID, true);
            player.networkHandler.sendPacket(new ResourcePackSendS2CPacket(packUUID, rpOption.url, rpOption.hash, rpOption.required, rpOption.hasPrompt ? Text.literal(rpOption.message) : null));
        }
    }

    public static void clearTempPacks(ServerPlayerEntity player) {
        if (playerLoaded.containsKey(player.getUuid()))
            playerLoaded.get(player.getUuid()).getPacks().entrySet().removeIf(uuidStatusEntry -> {
                if (uuidStatusEntry.getValue().isTempPack()) {
                    player.networkHandler.sendPacket(new ResourcePackRemoveS2CPacket(Optional.of(uuidStatusEntry.getKey())));
                    return true;
                }
                return false;
            });
    }
}