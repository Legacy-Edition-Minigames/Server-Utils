package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;

import com.google.gson.JsonArray;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TickMuteAction {

    private static int ticks;

    public static void tick(MinecraftServer server) {
        ticks++;
        if (ticks > 20 * 60 * 2) {
            ticks = 0;

            JsonArray obj = new JsonArray();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!ProhibitorModule.canChat(player) && !UserConfigStorage.getValue(player, new Identifier("muteduration")).equals("-1")) {
                    obj.add(player.getUuidAsString());
                }
            }

            if (!obj.isEmpty()) BackendServer.asyncPost("prohibitor/mute/tick", obj.toString());
        }
    }
}
