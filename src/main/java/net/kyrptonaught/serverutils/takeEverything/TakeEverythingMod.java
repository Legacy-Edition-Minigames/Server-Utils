package net.kyrptonaught.serverutils.takeEverything;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypedActionResult;

import java.util.Collection;


public class TakeEverythingMod extends ModuleWConfig<TakeEverythingConfig> {

    public void onInitialize() {
        TakeEverythingNetworking.registerReceivePacket();
        registerItemUse();
    }

    @Override
    public TakeEverythingConfig createDefaultConfig() {
        return new TakeEverythingConfig();
    }

    public static TakeEverythingConfig getConfigStatic() {
        return ServerUtilsMod.TakeEverythingModule.getConfig();
    }

    public static void registerItemUse() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient && TakeEverythingHelper.isSwappableItem(stack)) {
                stack = TakeEverythingHelper.equipOrSwapArmor(player, stack, true); //return already equippedStack or empty
                if (!stack.isEmpty()) player.setStackInHand(hand, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("takeeverything")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!TakeEverythingHelper.takeEverything(context.getSource().getPlayer()))
                        context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("You must have an inventory open"), false);
                    return 1;
                })
                .then(CommandManager.literal("enabled").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().Enabled = BoolArgumentType.getBool(context, "enabled");
                            saveConfig();
                            return 1;
                        })))
                .then(CommandManager.literal("worksInSpectator").then(CommandManager.argument("worksInSpectator", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().worksInSpectator = BoolArgumentType.getBool(context, "worksInSpectator");
                            saveConfig();
                            return 1;
                        })))
                .then(CommandManager.literal("deleteItemNotDrop").then(CommandManager.argument("deleteItemNotDrop", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().deleteItemNotDrop = BoolArgumentType.getBool(context, "deleteItemNotDrop");
                            saveConfig();
                            return 1;
                        })))
                .then(CommandManager.literal("ignoreEnchants")
                        .then(CommandManager.argument("ignoreEnchants", BoolArgumentType.bool())
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .executes(context -> execute(EntityArgumentType.getPlayers(context, "players"), BoolArgumentType.getBool(context, "ignoreEnchants")))))
                        .then(CommandManager.literal("clear").executes(context -> {
                            TakeEverythingConfig.SWAP_IGNORE_ENCHANTS.clear();
                            return 1;
                        })))
        );
    }

    private static int execute(Collection<ServerPlayerEntity> players, boolean enabled) {
        players.forEach(serverPlayerEntity -> {
            if (enabled) TakeEverythingConfig.SWAP_IGNORE_ENCHANTS.add(serverPlayerEntity.getUuidAsString());
            else TakeEverythingConfig.SWAP_IGNORE_ENCHANTS.remove(serverPlayerEntity.getUuidAsString());
        });

        return 1;
    }
}

