package net.kyrptonaught.serverutils.healthcmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public class HealthCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<ServerCommandSource> healthCMD = CommandManager.literal("health")
                .requires((source) -> source.hasPermissionLevel(2));

        dispatcher.register(healthCMD.then(CommandManager.argument("entity", EntityArgumentType.entities())
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg())
                                .executes((commandContext) -> execute(commandContext, HealthCMDMod.ModType.ADD, null)))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.ADD, null))))
                )));

        dispatcher.register(healthCMD.then(CommandManager.argument("entity", EntityArgumentType.entities())
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg())
                                .executes((commandContext) -> execute(commandContext, HealthCMDMod.ModType.SET, null)))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.SET, null))))
                )));

        dispatcher.register(healthCMD.then(CommandManager.argument("entity", EntityArgumentType.entities())
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg())
                                .then(CommandManager.argument("damageType", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.DAMAGE_TYPE))
                                        .executes((context) -> execute(context, HealthCMDMod.ModType.SUB, new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE))))))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .then(CommandManager.argument("damageType", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.DAMAGE_TYPE))
                                        .executes(context -> getScoreboard(context, HealthCMDMod.ModType.SUB, new DamageSource(RegistryEntryArgumentType.getRegistryEntry(context, "damageType", RegistryKeys.DAMAGE_TYPE)))))))
                )));

        dispatcher.register(healthCMD.then(CommandManager.argument("entity", EntityArgumentType.entities())
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("amount", FloatArgumentType.floatArg())
                                .then(CommandManager.literal("custom")
                                        .then(CommandManager.argument("deathMessage", StringArgumentType.string())
                                                .executes((context) -> execute(context, HealthCMDMod.ModType.SUB, null)))))
                        .then(CommandManager.literal("scoreboard").then(CommandManager.argument("obj", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .then(CommandManager.literal("custom")
                                        .then(CommandManager.argument("deathMessage", StringArgumentType.string())
                                                .executes(context -> getScoreboard(context, HealthCMDMod.ModType.SUB, null))))))
                )));
    }

    private static int getScoreboard(CommandContext<ServerCommandSource> commandContext, HealthCMDMod.ModType modType, DamageSource dmgSource) throws CommandSyntaxException {
        ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(commandContext, "obj");
        if (modType == HealthCMDMod.ModType.SUB && dmgSource == null)
            dmgSource = new CustomDeathMessage(commandContext.getSource().getServer().getRegistryManager(), StringArgumentType.getString(commandContext, "deathMessage"));

        for (PlayerEntity player : EntityArgumentType.getPlayers(commandContext, "entity")) {
            int amount = commandContext.getSource().getServer().getScoreboard().getPlayerScore(player.getEntityName(), obj).getScore();
            execute(player, amount, modType, dmgSource);
        }
        return 1;
    }

    private static int execute(CommandContext<ServerCommandSource> commandContext, HealthCMDMod.ModType modType, DamageSource dmgSource) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(commandContext, "amount");
        if (modType == HealthCMDMod.ModType.SUB && dmgSource == null)
            dmgSource = new CustomDeathMessage(commandContext.getSource().getServer().getRegistryManager(), StringArgumentType.getString(commandContext, "deathMessage"));

        for (Entity entity : EntityArgumentType.getEntities(commandContext, "entity")) {
            execute(entity, amount, modType, dmgSource);
        }
        return 1;
    }

    private static void execute(Entity entity, float amount, HealthCMDMod.ModType modType, DamageSource dmgSource) {
        if (modType == HealthCMDMod.ModType.ADD) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.heal(amount);
                if (entity instanceof ServerPlayerEntity player) {
                    player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
                }
            }

        } else if (modType == HealthCMDMod.ModType.SET) {
            if (entity instanceof LivingEntity livingEntity)
                livingEntity.setHealth(amount);

        } else if (modType == HealthCMDMod.ModType.SUB) {
            entity.damage(dmgSource, amount);
        }
    }
}