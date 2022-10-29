package net.kyrptonaught.serverutils;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public class Module {
    private String MOD_ID;

    public void setMOD_ID(String MOD_ID) {
        this.MOD_ID = MOD_ID;
    }

    public String getMOD_ID() {
        return MOD_ID;
    }

    public void onInitialize() {

    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

    }
}
