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

public class ArmorHudMod extends Module{
    public static final Identifier ARMOR_HUD_STATE = new Identifier("lem", "armor_hud_state_packet");

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("armorHud")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.player()).then(CommandManager.argument("state", BoolArgumentType.bool())
                        .executes(context -> {
                            ServerPlayerEntity entity = EntityArgumentType.getPlayer(context, "entity");
                            boolean state = BoolArgumentType.getBool(context, "state");
                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeBoolean(state);
                            if (entity!=null)
                                ServerPlayNetworking.send(entity, ARMOR_HUD_STATE,buf);
                            return 1;
                        }))));
    }
}
