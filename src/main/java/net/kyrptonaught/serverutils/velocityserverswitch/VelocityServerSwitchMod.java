package net.kyrptonaught.serverutils.velocityserverswitch;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ByteBufDataOutput;
import net.kyrptonaught.serverutils.Constants;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class VelocityServerSwitchMod extends Module {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("velocityserverswitch")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("servername", StringArgumentType.word())
                        .executes((commandContext) -> {
                            String servername = StringArgumentType.getString(commandContext, "servername");
                            try (ByteBufDataOutput output = new ByteBufDataOutput(new PacketByteBuf(Unpooled.buffer()))) {

                                output.writeUTF("Connect");
                                output.writeUTF(servername);
                                ServerPlayNetworking.send(commandContext.getSource().getPlayer(), Constants.BUNGEECORD_ID, output.getBuf());
                            } catch (Exception e) {
                                System.out.println("Failed to send switch packet");
                                e.printStackTrace();
                            }
                            return 1;
                        })));
    }
}
