package net.kyrptonaught.serverutils.ride;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RideMod extends Module {

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ride")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                        .executes(context -> {
                            Entity entity = EntityArgumentType.getEntity(context, "entity");
                            PlayerEntity player = context.getSource().getPlayer();
                            if (entity != player && entity != null && player != null)
                                player.startRiding(entity);
                            else
                                context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("You cannot ride yourself"), false);
                            return 1;
                        })));
    }
}