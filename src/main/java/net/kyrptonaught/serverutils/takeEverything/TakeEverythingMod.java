package net.kyrptonaught.serverutils.takeEverything;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.TypedActionResult;

import java.util.Collection;


public class TakeEverythingMod {
    public static final String MOD_ID = "takeeverything";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new TakeEverythingConfig());
        CommandRegistrationCallback.EVENT.register(TakeEverythingMod::registerCommand);
        TakeEverythingNetworking.registerReceivePacket();
        registerItemUse();
    }

    public static TakeEverythingConfig getConfig() {
        return (TakeEverythingConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }

    public static void registerItemUse() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient && stack.getItem() instanceof ArmorItem) {
                stack = TakeEverythingHelper.equipOrSwapArmor(player, stack, true); //return already equippedStack or empty
                if (!stack.isEmpty()) player.setStackInHand(hand, stack);
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("takeeverything")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!TakeEverythingHelper.takeEverything(context.getSource().getPlayer()))
                        context.getSource().sendFeedback(new LiteralText("You must have an inventory open"), false);
                    return 1;
                })
                .then(CommandManager.literal("enabled").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().Enabled = BoolArgumentType.getBool(context, "enabled");
                            ServerUtilsMod.configManager.save(MOD_ID);
                            return 1;
                        })))
                .then(CommandManager.literal("worksInSpectator").then(CommandManager.argument("worksInSpectator", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().worksInSpectator = BoolArgumentType.getBool(context, "worksInSpectator");
                            ServerUtilsMod.configManager.save(MOD_ID);
                            return 1;
                        })))
                .then(CommandManager.literal("deleteItemNotDrop").then(CommandManager.argument("deleteItemNotDrop", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().deleteItemNotDrop = BoolArgumentType.getBool(context, "deleteItemNotDrop");
                            ServerUtilsMod.configManager.save(MOD_ID);
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

