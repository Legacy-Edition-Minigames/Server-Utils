package net.kyrptonaught.serverutils.healthcmd;


import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class HealthCMDMod {
    public enum ModType {
        ADD, SUB, SET
    }

    public static final String MOD_ID = "healthcmd";

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(HealthCommand::registerCommand);
        CommandRegistrationCallback.EVENT.register(HungerCommand::registerCommand);
        CommandRegistrationCallback.EVENT.register(SaturationCommand::registerCommand);
    }
}
