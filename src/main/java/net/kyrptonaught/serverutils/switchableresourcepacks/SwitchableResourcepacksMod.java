package net.kyrptonaught.serverutils.switchableresourcepacks;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
        STARTED = Criteria.register(new CustomCriterion("started"));
        FINISHED = Criteria.register(new CustomCriterion("finished"));
        FAILED = Criteria.register(new CustomCriterion("failed"));
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
        ResourcePackConfig.RPOption rpOption = rpOptionHashMap.get(packname);
        if (rpOption == null) {
            commandContext.getSource().sendFeedback(Text.literal("Packname: ").append(packname).append(" was not found"), false);
            return 1;
        }
        players.forEach(player -> {
            if (getConfig().autoRevoke) {
                STARTED.revoke(player);
                FINISHED.revoke(player);
                FAILED.revoke(player);
            }

            player.sendResourcePackUrl(rpOption.url, rpOption.hash, rpOption.required, rpOption.hasPrompt ? Text.literal(rpOption.message) : null);
        });
        commandContext.getSource().sendFeedback(Text.literal("Enabled pack: ").append(rpOption.packname), false);
        return 1;
    }
}