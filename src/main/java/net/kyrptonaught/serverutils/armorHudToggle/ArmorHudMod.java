package net.kyrptonaught.serverutils.armorHudToggle;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class ArmorHudMod extends Module {
    private static final Identifier ARMOR_HUD_ENABLE = new Identifier("armorhud", "armor_hud_render_enable");
    private static final Identifier ARMOR_HUD_DISABLE = new Identifier("armorhud", "armor_hud_render_disable");

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("armorHud")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.players()).then(CommandManager.argument("state", BoolArgumentType.bool())
                        .executes(context -> {
                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "entity");
                            boolean state = BoolArgumentType.getBool(context, "state");

                            if (players != null)
                                for (ServerPlayerEntity player : players) {
                                    ServerPlayNetworking.send(player, state ? ARMOR_HUD_ENABLE : ARMOR_HUD_DISABLE, PacketByteBufs.create());
                                }
                            return 1;
                        }))));
    }
}