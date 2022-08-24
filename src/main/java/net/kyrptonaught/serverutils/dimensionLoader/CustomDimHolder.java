package net.kyrptonaught.serverutils.dimensionLoader;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.Collection;

public class CustomDimHolder {
    public Identifier dimID;
    public Identifier copyFromID;
    private Collection<CommandFunction> functions;

    public RuntimeWorldHandle world;
    private boolean scheduleDelete = false;

    public CustomDimHolder(Identifier dimID, Identifier copyFromID, Collection<CommandFunction> functions) {
        this.dimID = dimID;
        this.copyFromID = copyFromID;
        this.functions = functions;
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

    public void setFunctions(Collection<CommandFunction> functions) {
        this.functions = functions;
    }

    public void executeFunctions(MinecraftServer server) {
        if (functions != null)
            for (CommandFunction commandFunction : functions) {
                server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
            }
    }
}
