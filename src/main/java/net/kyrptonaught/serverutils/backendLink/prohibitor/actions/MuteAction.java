package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorCommands;
import net.minecraft.text.Text;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.function.Consumer;

public class MuteAction {
    public static int permMute(Collection<GameProfile> players, String reason, String who, Consumer<Text> responder) {
        JsonObject stamp = ProhibitorCommands.stamp(reason, who);

        for (GameProfile player : players) {
            BackendServer.asyncPost("prohibitor/mute/perm/" + player.getId(), stamp.toString(), (success, response) -> {
                if (!success) responder.accept(Text.literal("Error Punishing Player"));
            });
        }

        return 1;
    }

    public static int tempMute(Collection<GameProfile> players, String reason, String who, ChronoUnit unit, int durationTime, Consumer<Text> responder) {
        JsonObject stamp = ProhibitorCommands.stamp(reason, who);
        stamp.addProperty("duration_time", durationTime);
        stamp.addProperty("duration_type", unit.ordinal());

        for (GameProfile player : players) {
            BackendServer.asyncPost("prohibitor/mute/temp/" + player.getId(), stamp.toString(), (success, response) -> {
                if (!success) responder.accept(Text.literal("Error Punishing Player"));
            });
        }

        return 1;
    }
}
