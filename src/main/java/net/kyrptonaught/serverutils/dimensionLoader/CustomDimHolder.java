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

    public CustomDimHolder(Identifier dimID, Identifier copyFromID) {
        this.dimID = dimID;
        this.copyFromID = copyFromID;
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

    public CustomDimHolder setCompleteTask(BiConsumer<MinecraftServer, CustomDimHolder> execute) {
        this.completionTask = execute;
        return this;
    }

    public CustomDimHolder setCompleteTask(Collection<CommandFunction<ServerCommandSource>> functions) {
        setCompleteTask((server, customDimHolder) -> {
            if (functions != null) {
                for (CommandFunction<ServerCommandSource> commandFunction : functions) {
                    server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
                }
            }
        });
        return this;
    }

    public void executeComplete(MinecraftServer server) {
        if (completionTask != null) completionTask.accept(server, this);
    }
}
