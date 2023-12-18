package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class SwitchableResourcepacksMod extends ModuleWConfig<ResourcePackConfig> {

    public static HashMap<String, ResourcePackConfig.RPOption> rpOptionHashMap = new HashMap<>();
    public static CustomCriterion STARTED, FINISHED, FAILED;

    public void onConfigLoad(ResourcePackConfig config) {
        rpOptionHashMap.clear();
        config.packs.forEach(rpOption -> {
            rpOptionHashMap.put(rpOption.packname, rpOption);
        });

        if (config.packs.size() == 0) {
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
        STARTED = registerCriterion(new Identifier(ServerUtilsMod.SwitchableResourcepacksModule.getMOD_ID(), "started"));
        FINISHED = registerCriterion(new Identifier(ServerUtilsMod.SwitchableResourcepacksModule.getMOD_ID(), "finished"));
        FAILED = registerCriterion(new Identifier(ServerUtilsMod.SwitchableResourcepacksModule.getMOD_ID(), "failed"));
    }

    private CustomCriterion registerCriterion(Identifier id) {
        return Criteria.register(id.toString(), new CustomCriterion(id));
    }

    @Override
    public ResourcePackConfig createDefaultConfig() {
        return new ResourcePackConfig();
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

    public boolean execute(String packname, Collection<ServerPlayerEntity> players) {
        ResourcePackConfig.RPOption rpOption = rpOptionHashMap.get(packname);
        if (rpOption == null) {
            return false;
        }
        players.forEach(player -> {
            if (getConfig().autoRevoke) {
                grantAdvancement(player, STARTED);
                grantAdvancement(player, FINISHED);
                grantAdvancement(player, FAILED);
            }

            //todo UUIDs
            ResourcePackSendS2CPacket resourcePackSendS2CPacket = new ResourcePackSendS2CPacket(UUID.nameUUIDFromBytes(rpOption.packname.getBytes(StandardCharsets.UTF_8)), rpOption.url, rpOption.hash, rpOption.required, rpOption.hasPrompt ? Text.literal(rpOption.message) : null);
            player.networkHandler.sendPacket(resourcePackSendS2CPacket);
        });
        return true;
    }

    public static void grantAdvancement(ServerPlayerEntity player, CustomCriterion customCriterion) {
        player.getServer().getAdvancementLoader().getAdvancements().forEach(advancement -> {
            advancement.value().criteria().forEach((s, advancementCriterion) -> {
                if (advancementCriterion.trigger() instanceof CustomCriterion testCriterion && testCriterion.id.equals(customCriterion.id))
                    player.getAdvancementTracker().grantCriterion(advancement, s);
            });
        });
    }

    public static void revokeAdvancement(ServerPlayerEntity player, CustomCriterion customCriterion) {
        player.getServer().getAdvancementLoader().getAdvancements().forEach(advancement -> {
            advancement.value().criteria().forEach((s, advancementCriterion) -> {
                if (advancementCriterion.trigger() instanceof CustomCriterion testCriterion && testCriterion.id.equals(customCriterion.id))
                    player.getAdvancementTracker().revokeCriterion(advancement, s);
            });
        });
    }
}