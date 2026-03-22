package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorCommands;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.Consumer;

public class SusAction {
    public static int sus(Collection<GameProfile> players, String reason, String who, Consumer<Text> responder) {
        JsonObject stamp = ProhibitorCommands.stamp(reason, who);

        for (GameProfile player : players) {
            BackendServer.asyncPost("prohibitor/sus/add/" + player.getId(), stamp.toString(), (success, response) -> {
                if (!success) responder.accept(Text.literal("Error Punishing Player"));
            });

        }
        return 1;
    }
}
