package net.kyrptonaught.serverutils.ride;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class RideMod {
    public static void onInitialize() {

        CommandRegistrationCallback.EVENT.register(RideMod::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("ride")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                        .executes(context -> {
                            Entity entity = EntityArgumentType.getEntity(context, "entity");
                            PlayerEntity player = context.getSource().getPlayer();
                            if (entity != player)
                                player.startRiding(entity);
                            else context.getSource().sendFeedback(new LiteralText("You cannot ride yourself"), false);
                            return 1;
                        })));
    }
}