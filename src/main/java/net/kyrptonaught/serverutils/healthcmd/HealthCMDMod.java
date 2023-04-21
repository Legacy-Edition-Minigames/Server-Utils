package net.kyrptonaught.serverutils.healthcmd;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;

public class HealthCMDMod extends Module {
    public enum ModType {
        ADD, SUB, SET
    }

    public static final String MOD_ID = "healthcmd";

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        HealthCommand.registerCommands(dispatcher, registryAccess);
        HungerCommand.registerCommands(dispatcher);
        SaturationCommand.registerCommands(dispatcher);
    }
}
