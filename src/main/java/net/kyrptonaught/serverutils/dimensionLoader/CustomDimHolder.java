package net.kyrptonaught.serverutils.dimensionLoader;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Collection;
import java.util.function.BiConsumer;

public class CustomDimHolder {
    public Identifier dimID;
    public Identifier copyFromID;
    private BiConsumer<MinecraftServer, CustomDimHolder> completionTask;

    public RuntimeWorldHandle world;
    private boolean scheduleDelete = false;

    public CustomDimHolder(Identifier dimID, Identifier copyFromID, Collection<CommandFunction<ServerCommandSource>> functions) {
        this.dimID = dimID;
        this.copyFromID = copyFromID;
        setFunctions(functions);
    }

    public CustomDimHolder(Identifier dimID, Identifier copyFromID, BiConsumer<MinecraftServer, CustomDimHolder> functions) {
        this.dimID = dimID;
        this.copyFromID = copyFromID;
        setFunctions(functions);
    }

    public void scheduleToDelete() {
        this.scheduleDelete = true;
    }

    public boolean scheduledDelete() {
        return scheduleDelete;
    }

    public boolean deleteFinished(Fantasy fantasy) {
        if (!scheduledDelete()) return false;

        if (wasRegistered())
            return fantasy.tickDeleteWorld(world.asWorld());

        return true;
    }

    public boolean wasRegistered() {
        return this.world != null;
    }

    public void register(RuntimeWorldHandle handle) {
        this.world = handle;
    }

    public void setFunctions(BiConsumer<MinecraftServer, CustomDimHolder> execute) {
        this.completionTask = execute;
    }

    public void setFunctions(Collection<CommandFunction<ServerCommandSource>> functions) {
        setFunctions((server, customDimHolder) -> {
            if (functions != null) {
                for (CommandFunction<ServerCommandSource> commandFunction : functions) {
                    server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
                }
            }
        });
    }

    public void executeFunctions(MinecraftServer server) {
        if (completionTask != null) completionTask.accept(server, this);
    }
}
