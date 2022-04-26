package net.kyrptonaught.serverutils.healthcmd;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class HealthCMDMod {
    public enum ModType {
        ADD, SUB, SET
    }

    public static final String MOD_ID = "healthcmd";

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(HealthCommand::register);
        CommandRegistrationCallback.EVENT.register(HungerCommand::register);
        CommandRegistrationCallback.EVENT.register(SaturationCommand::register);
    }
}
