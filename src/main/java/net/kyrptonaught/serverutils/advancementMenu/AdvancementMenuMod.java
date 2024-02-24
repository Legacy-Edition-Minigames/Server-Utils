package net.kyrptonaught.serverutils.advancementMenu;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdvancementMenuMod extends Module {
    public static String EXECUTE_COMMAND;

    @Override
    public void onInitialize() {
        super.onInitialize();

    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("advancementmenu")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("runCommand", StringArgumentType.greedyString())
                                .executes((context -> {
                                    EXECUTE_COMMAND = StringArgumentType.getString(context, "runCommand");
                                    return 1;
                                }))
                        ))
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            EXECUTE_COMMAND = null;
                            return 1;
                        })));
    }

    public static void executeCommand(ServerPlayerEntity player) {
        CMDHelper.executeAs(player, EXECUTE_COMMAND);
    }
}
