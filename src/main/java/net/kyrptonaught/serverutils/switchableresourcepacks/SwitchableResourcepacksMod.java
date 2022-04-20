package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.advancement.CriterionRegistry;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class SwitchableResourcepacksMod {
    public static final String MOD_ID = "switchableresourcepacks";

    public static HashMap<String, ResourcePackConfig.RPOption> rpOptionHashMap = new HashMap<>();
    public static CustomCriterion STARTED, FINISHED, FAILED;

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new ResourcePackConfig());
        ServerUtilsMod.configManager.load(MOD_ID);

        getConfig().packs.forEach(rpOption -> {
            rpOptionHashMap.put(rpOption.packname, rpOption);
        });

        CommandRegistrationCallback.EVENT.register(SwitchableResourcepacksMod::register);

        if (getConfig().packs.size() == 0) {
            ResourcePackConfig.RPOption option = new ResourcePackConfig.RPOption();
            option.packname = "example_pack";
            option.url = "https://example.com/resourcepack.zip";
            option.hash = "examplehash";
            getConfig().packs.add(option);
            ServerUtilsMod.configManager.save(MOD_ID);
            System.out.println("[" + MOD_ID + "]: Generated example resourcepack config");
        }

        STARTED = CriterionRegistry.register(new CustomCriterion("started"));
        FINISHED = CriterionRegistry.register(new CustomCriterion("finished"));
        FAILED = CriterionRegistry.register(new CustomCriterion("failed"));
    }

    public static ResourcePackConfig getConfig() {
        return ((ResourcePackConfig) ServerUtilsMod.configManager.getConfig(MOD_ID));
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("loadresource").requires((source) -> source.hasPermissionLevel(0));
        for (String packname : rpOptionHashMap.keySet()) {
            cmd.then(CommandManager.literal(packname)
                    .then(CommandManager.argument("player", EntityArgumentType.players())
                            .executes(commandContext -> execute(commandContext, packname, EntityArgumentType.getPlayers(commandContext, "player"))))
                    .executes(commandContext -> execute(commandContext, packname, Collections.singleton(commandContext.getSource().getPlayer()))));
        }
        dispatcher.register(cmd);
    }

    public static int execute(CommandContext<ServerCommandSource> commandContext, String packname, Collection<ServerPlayerEntity> players) {
        ResourcePackConfig.RPOption rpOption = rpOptionHashMap.get(packname);
        if (rpOption == null) {
            commandContext.getSource().sendFeedback(new LiteralText("Packname: ").append(packname).append(" was not found"), false);
            return 1;
        }
        players.forEach(player -> {
            if (getConfig().autoRevoke) {
                STARTED.revoke(player);
                FINISHED.revoke(player);
                FAILED.revoke(player);
            }

            player.sendResourcePackUrl(rpOption.url, rpOption.hash, rpOption.required, rpOption.hasPrompt ? new LiteralText(rpOption.message) : null);
        });
        commandContext.getSource().sendFeedback(new LiteralText("Enabled pack: ").append(rpOption.packname), false);
        return 1;
    }
}