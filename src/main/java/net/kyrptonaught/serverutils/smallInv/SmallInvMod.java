package net.kyrptonaught.serverutils.smallInv;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.takeEverything.TakeEverythingHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SmallInvMod extends Module {

    public static boolean ENABLED = false;

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("smallinv")
                .requires((source) -> source.hasPermissionLevel(2))

                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            ENABLED = BoolArgumentType.getBool(context, "enabled");
                            return 1;
                        })));
    }

    public static void executeClicked(ServerPlayerEntity player) {
        TakeEverythingHelper.takeEverything(player);
    }

    public static boolean isSmallSlot(ItemStack stack) {
        return stack.isOf(Items.KNOWLEDGE_BOOK) &&
                stack.hasNbt() &&
                stack.getNbt().contains("SmallInv") &&
                stack.getNbt().getInt("SmallInv") == 1;
    }
}
