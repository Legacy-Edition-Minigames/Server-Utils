package net.kyrptonaught.serverutils.velocitymodifier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class VelocityCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("modifyvelocity")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.entities())
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes((commandContext) -> execute(commandContext, EntityArgumentType.getEntities(commandContext, "entity"), false)
                                                        )))))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                                        .executes((commandContext) -> execute(commandContext, EntityArgumentType.getEntities(commandContext, "entity"), true)
                                                        )))))));

        dispatcher.register(CommandManager.literal("velocityforward")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.entities())
                        .then(CommandManager.argument("speed", DoubleArgumentType.doubleArg())
                                .executes((commandContext) -> {
                                    double speed = DoubleArgumentType.getDouble(commandContext, "speed");
                                    EntityArgumentType.getEntities(commandContext, "entity").forEach(entity -> {
                                        Vec3d vec3d = entity.getRotationVector();
                                        Vec3d vec3d2 = entity.getVelocity();
                                        entity.setVelocity(vec3d2.add(vec3d.x * 0.1D + (vec3d.x * 1.5D - vec3d2.x) * speed, vec3d.y * 0.1D + (vec3d.y * 1.5D - vec3d2.y) * speed, vec3d.z * 0.1D + (vec3d.z * 1.5D - vec3d2.z) * speed));
                                        entity.velocityModified = true;
                                    });
                                    return 1;
                                }))));
    }

    private static int execute(CommandContext<ServerCommandSource> commandContext, Collection<? extends Entity> entities, Boolean set) {
        double x = DoubleArgumentType.getDouble(commandContext, "x");
        double y = DoubleArgumentType.getDouble(commandContext, "y");
        double z = DoubleArgumentType.getDouble(commandContext, "z");
        entities.forEach(entity -> {
            if (set)
                entity.setVelocity(x, y, z);
            else
                entity.addVelocity(x, y, z);
            entity.velocityModified = true;
        });
        return 1;
    }
}