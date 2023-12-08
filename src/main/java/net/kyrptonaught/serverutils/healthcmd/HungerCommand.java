package net.kyrptonaught.serverutils.healthcmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.MathHelper;


public class HungerCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> healthCMD = CommandManager.literal("hunger")
                .requires((source) -> source.hasPermissionLevel(2));

        dispatcher.register(healthCMD.then(CommandManager.argument("player", EntityArgumentType.players())
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes((commandContext) -> execute(commandContext, HealthCMDMod.ModType.ADD)))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.ADD))))
                )));

        dispatcher.register(healthCMD.then(CommandManager.argument("player", EntityArgumentType.entities())
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes((commandContext) -> execute(commandContext, HealthCMDMod.ModType.SET)))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.SET))))
                )));

        dispatcher.register(healthCMD.then(CommandManager.argument("player", EntityArgumentType.entities())
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                .executes((commandContext) -> execute(commandContext, HealthCMDMod.ModType.SUB)))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.SUB))))
                )));
    }

    private static int getScoreboard(CommandContext<ServerCommandSource> commandContext, HealthCMDMod.ModType modType) throws CommandSyntaxException {
        ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(commandContext, "obj");
        EntityArgumentType.getPlayers(commandContext, "player").forEach(serverPlayerEntity -> {
            int amount = commandContext.getSource().getServer().getScoreboard().getOrCreateScore(ScoreHolder.fromName(serverPlayerEntity.getNameForScoreboard()), obj).getScore();
            execute(serverPlayerEntity, amount, modType);
        });
        return 1;
    }

    private static int execute(CommandContext<ServerCommandSource> commandContext, HealthCMDMod.ModType modType) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(commandContext, "amount");
        EntityArgumentType.getPlayers(commandContext, "player").forEach(player -> execute(player, amount, modType));
        return 1;
    }

    private static void execute(PlayerEntity player, int amount, HealthCMDMod.ModType modType) {
        if (modType == HealthCMDMod.ModType.SET) {
            player.getHungerManager().setFoodLevel(MathHelper.clamp(amount, 0, 20));

        } else if (modType == HealthCMDMod.ModType.SUB) {
            player.getHungerManager().setFoodLevel(MathHelper.clamp(player.getHungerManager().getFoodLevel() - amount, 0, 20));

        } else if (modType == HealthCMDMod.ModType.ADD) {
            player.getHungerManager().setFoodLevel(MathHelper.clamp(player.getHungerManager().getFoodLevel() + amount, 0, 20));
        }
    }
}