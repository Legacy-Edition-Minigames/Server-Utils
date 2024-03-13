package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class SwitchableResourcepacksMod extends ModuleWConfig<ResourcePackConfig> {
    public static final HashMap<Identifier, ResourcePackConfig.RPOption> rpOptionHashMap = new HashMap<>();

    private static final HashMap<UUID, PackStatus> playerLoaded = new HashMap<>();

    private static Collection<CommandFunction<ServerCommandSource>> RP_FAILED_FUNCTIONS, RP_LOADED_FUNCTIONS, RP_STARTED_FUNCTIONS;
    private static Identifier CUSTOMPACKID = new Identifier("custompack", "enabled");
    private static UUID CUSTOMPACKUUID = UUID.nameUUIDFromBytes(CUSTOMPACKID.toString().getBytes(StandardCharsets.UTF_8));

    public void onConfigLoad(ResourcePackConfig config) {
        RP_FAILED_FUNCTIONS = null;
        RP_LOADED_FUNCTIONS = null;
        rpOptionHashMap.clear();
        config.packs.forEach(rpOption -> rpOptionHashMap.put(rpOption.packID, rpOption));
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerLoaded.remove(handler.getPlayer().getUuid()));
    }

    @Override
    public ResourcePackConfig createDefaultConfig() {
        return new ResourcePackConfig();
    }

    public static boolean allPacksLoaded(ServerPlayerEntity player) {
        if (!playerLoaded.containsKey(player.getUuid())) return true;

        PackStatus packs = playerLoaded.get(player.getUuid());
        for (UUID pack : packs.getPacks().keySet()) {
            if (!packs.isComplete(pack))
                return false;
        }

        return true;
    }

    public static boolean didPackFail(ServerPlayerEntity player) {
        if (!playerLoaded.containsKey(player.getUuid())) return false;

        PackStatus packs = playerLoaded.get(player.getUuid());
        for (UUID pack : packs.getPacks().keySet()) {
            if (packs.didFail(pack))
                return true;
        }

        return false;
    }

    public static void packStatusUpdate(ServerPlayerEntity player, UUID packname, PackStatus.LoadingStatus status) {
        if (!playerLoaded.containsKey(player.getUuid()))
            playerLoaded.put(player.getUuid(), new PackStatus());

        playerLoaded.get(player.getUuid()).setPackLoadStatus(packname, status);

        if (allPacksLoaded(player)) {
            if (RP_LOADED_FUNCTIONS == null)
                RP_LOADED_FUNCTIONS = getFunctions(player.getServer(), ServerUtilsMod.SwitchableResourcepacksModule.getConfig().playerCompleteFunction);
            if (RP_FAILED_FUNCTIONS == null)
                RP_FAILED_FUNCTIONS = getFunctions(player.getServer(), ServerUtilsMod.SwitchableResourcepacksModule.getConfig().playerFailedFunction);
            if (RP_STARTED_FUNCTIONS == null)
                RP_STARTED_FUNCTIONS = getFunctions(player.getServer(), ServerUtilsMod.SwitchableResourcepacksModule.getConfig().playerStartFunction);

            if (didPackFail(player))
                CMDHelper.executeAs(player, RP_FAILED_FUNCTIONS);
            else
                CMDHelper.executeAs(player, RP_LOADED_FUNCTIONS);
        }
    }

    private static Collection<CommandFunction<ServerCommandSource>> getFunctions(MinecraftServer server, String id) {
        if (id.startsWith("#")) {
            return server.getCommandFunctionManager().getTag(new Identifier(id.replaceAll("#", "")));
        }

        return Collections.singleton(server.getCommandFunctionManager().getFunction(new Identifier(id)).get());
    }

    private static void addPackStatus(ServerPlayerEntity player, UUID packname, boolean temp) {
        if (!playerLoaded.containsKey(player.getUuid()))
            playerLoaded.put(player.getUuid(), new PackStatus());

        playerLoaded.get(player.getUuid()).addPack(packname, temp);
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("loadresource")
                .requires((source) -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("packid", IdentifierArgumentType.identifier())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            Identifier packID = IdentifierArgumentType.getIdentifier(context, "packid");
                            ResourcePackConfig.RPOption rpOption = rpOptionHashMap.get(packID);
                            if (rpOption == null) {
                                context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Packname: " + packID + " was not found"), false);
                            } else {
                                execute(rpOption, player);
                                context.getSource().sendFeedback(CMDHelper.getFeedback(Text.literal("Enabled pack: ").append(rpOption.getNameText())), false);
                            }
                            return 1;
                        })));

        dispatcher.register(CommandManager.literal("custompack")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            ServerPlayerEntity player = context.getSource().getPlayer();

                            UserConfigStorage.setValue(player, CUSTOMPACKID, String.valueOf(enabled));
                            UserConfigStorage.syncPlayer(player);

                            if (playerLoaded.containsKey(player.getUuid()))
                                playerLoaded.get(player.getUuid()).getPacks().remove(CUSTOMPACKUUID);

                            player.sendMessage(Text.translatable(enabled ? "lem.config.custompack.enable" : "lem.config.custompack.disable"));
                            return 1;
                        })));
    }

    public static boolean isCustomPackEnabled(ServerPlayerEntity player) {
        return Boolean.parseBoolean(UserConfigStorage.getValue(player, CUSTOMPACKID));
    }

    private void execute(ResourcePackConfig.RPOption rpOption, ServerPlayerEntity player) {
        if (RP_STARTED_FUNCTIONS == null)
            RP_STARTED_FUNCTIONS = getFunctions(player.getServer(), ServerUtilsMod.SwitchableResourcepacksModule.getConfig().playerStartFunction);

        CMDHelper.executeAs(player, RP_STARTED_FUNCTIONS);

        if (isCustomPackEnabled(player)) {
            addPackStatus(player, CUSTOMPACKUUID, false);
            packStatusUpdate(player, CUSTOMPACKUUID, PackStatus.LoadingStatus.FINISHED);
            return;
        }

        UUID packUUID = UUID.nameUUIDFromBytes(rpOption.packID.toString().getBytes(StandardCharsets.UTF_8));
        if (playerLoaded.containsKey(player.getUuid()) && playerLoaded.get(player.getUuid()).getPacks().containsKey(packUUID))
            return;

        addPackStatus(player, packUUID, false);
        player.networkHandler.sendPacket(new ResourcePackSendS2CPacket(packUUID, rpOption.url, rpOption.hash, true, Text.literal(getConfig().message)));
    }

    public static void addPacks(List<ResourcePackConfig.RPOption> packList, ServerPlayerEntity player) {
        if (!hasNewPacks(packList, player)) return;

        if (RP_STARTED_FUNCTIONS == null)
            RP_STARTED_FUNCTIONS = getFunctions(player.getServer(), ServerUtilsMod.SwitchableResourcepacksModule.getConfig().playerStartFunction);

        CMDHelper.executeAs(player, RP_STARTED_FUNCTIONS);

        if (isCustomPackEnabled(player)) {
            addPackStatus(player, CUSTOMPACKUUID, false);
            packStatusUpdate(player, CUSTOMPACKUUID, PackStatus.LoadingStatus.FINISHED);
            return;
        }

        clearTempPacks(player);

        for (ResourcePackConfig.RPOption rpOption : packList) {
            UUID packUUID = UUID.nameUUIDFromBytes(rpOption.packID.toString().getBytes(StandardCharsets.UTF_8));
            addPackStatus(player, packUUID, true);
            player.networkHandler.sendPacket(new ResourcePackSendS2CPacket(packUUID, rpOption.url, rpOption.hash, true, Text.literal(ServerUtilsMod.SwitchableResourcepacksModule.getConfig().message)));
        }
    }

    private static void clearTempPacks(ServerPlayerEntity player) {
        if (playerLoaded.containsKey(player.getUuid()))
            playerLoaded.get(player.getUuid()).getPacks().entrySet().removeIf(uuidStatusEntry -> {
                if (uuidStatusEntry.getValue().isTempPack()) {
                    player.networkHandler.sendPacket(new ResourcePackRemoveS2CPacket(Optional.of(uuidStatusEntry.getKey())));
                    return true;
                }
                return false;
            });
    }

    private static boolean hasNewPacks(List<ResourcePackConfig.RPOption> packList, ServerPlayerEntity player) {

        List<UUID> c = new ArrayList<>();
        playerLoaded.get(player.getUuid()).getPacks().forEach((uuid, status) -> {
            if(status.isTempPack())
                c.add(uuid);
        });

        List<UUID> n = new ArrayList<>();
        for (ResourcePackConfig.RPOption rpOption : packList) {
            UUID packUUID = UUID.nameUUIDFromBytes(rpOption.packID.toString().getBytes(StandardCharsets.UTF_8));
            n.add(packUUID);
        }

        if (c.size() != n.size())
            return true;

        Collections.sort(c);
        Collections.sort(n);

        return !c.equals(n);
    }
}