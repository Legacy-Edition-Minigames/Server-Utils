package net.kyrptonaught.serverutils.snowballKnockback;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SnowballKnockbackMod extends ModuleWConfig<SnowballKnockbackConfig> {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> knockbackCommand = CommandManager.literal("snowkb")
                .requires(executor -> executor.hasPermissionLevel(2));

        knockbackCommand
                .then(CommandManager.literal("knockback")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("mult", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().snowKnockbackMult = FloatArgumentType.getFloat(cmd, "mult");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set knockback multiplier to " + getConfig().snowKnockbackMult), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Knockback multiplier is " + getConfig().snowKnockbackMult), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("added_mult", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().snowKnockbackMult += FloatArgumentType.getFloat(cmd, "added_mult");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set knockback multiplier to " + getConfig().snowKnockbackMult), false);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("damage")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().snowDamage = FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().snowDamage), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Damage is set to  " + getConfig().snowDamage), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().snowDamage += FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().snowDamage), false);
                                            return 1;
                                        }))));
        dispatcher.register(knockbackCommand);

        LiteralArgumentBuilder<ServerCommandSource> eggKnockbackCommand = CommandManager.literal("eggkb")
                .requires(executor -> executor.hasPermissionLevel(2));

        eggKnockbackCommand
                .then(CommandManager.literal("knockback")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("mult", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().eggKnockBackMult = FloatArgumentType.getFloat(cmd, "mult");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set knockback multiplier to " + getConfig().eggKnockBackMult), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Knockback multiplier is " + getConfig().eggKnockBackMult), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("added_mult", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().eggKnockBackMult += FloatArgumentType.getFloat(cmd, "added_mult");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set knockback multiplier to " + getConfig().eggKnockBackMult), false);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("damage")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().eggDamage = FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().eggDamage), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Damage is set to  " + getConfig().eggDamage), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().eggDamage += FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().eggDamage), false);
                                            return 1;
                                        }))));
        dispatcher.register(eggKnockbackCommand);

        LiteralArgumentBuilder<ServerCommandSource> bobber = CommandManager.literal("bobber").requires(executor -> executor.hasPermissionLevel(2));

        bobber.then(CommandManager.literal("pull")
                        .then(CommandManager.literal("set").then(CommandManager.argument("value", FloatArgumentType.floatArg())
                                .executes(cmd -> {
                                    getConfig().fishingRodPullMult = FloatArgumentType.getFloat(cmd, "value");
                                    saveConfig();
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set pull multiplier to " + getConfig().fishingRodPullMult), false);
                                    return 1;
                                })))
                        .then(CommandManager.literal("add").then(CommandManager.argument("value", FloatArgumentType.floatArg())
                                .executes(cmd -> {
                                    getConfig().fishingRodPullMult += FloatArgumentType.getFloat(cmd, "value");
                                    saveConfig();
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set pull multiplier to " + getConfig().fishingRodPullMult), false);
                                    return 1;
                                })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Pull multiplier is " + getConfig().fishingRodPullMult), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("damage")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().fishingRodDamage = FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().fishingRodDamage), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("get")
                                .executes(cmd -> {
                                    cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Damage is set to  " + getConfig().fishingRodDamage), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("damage", FloatArgumentType.floatArg())
                                        .executes(cmd -> {
                                            getConfig().fishingRodDamage += FloatArgumentType.getFloat(cmd, "damage");
                                            saveConfig();
                                            cmd.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Set damage to " + getConfig().fishingRodDamage), false);
                                            return 1;
                                        }))));
        dispatcher.register(bobber);
    }

    @Override
    public SnowballKnockbackConfig createDefaultConfig() {
        return new SnowballKnockbackConfig();
    }
}
