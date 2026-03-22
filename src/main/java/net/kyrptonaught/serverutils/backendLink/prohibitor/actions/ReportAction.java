package net.kyrptonaught.serverutils.backendLink.prohibitor.actions;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;


public class ReportAction {

    public static void report(ServerPlayerEntity reportingPlayer, GameProfile reportedPlayer, String category, String comment, boolean ping) {
        JsonObject stamp = ProhibitorCommands.stamp("{" + category + "}: " + comment, reportingPlayer.getNameForScoreboard() + "(" + reportingPlayer.getUuid() + ")");
        stamp.add("reporting_player", Codecs.GAME_PROFILE_WITH_PROPERTIES.encodeStart(JsonOps.INSTANCE, reportingPlayer.getGameProfile()).get().orThrow());
        stamp.add("reported_player", Codecs.GAME_PROFILE_WITH_PROPERTIES.encodeStart(JsonOps.INSTANCE, reportedPlayer).get().orThrow());
        stamp.addProperty("ping", ping);

        BackendServer.asyncPost("prohibitor/report/" + reportedPlayer.getId(), stamp.toString(), (success, response) -> {
            if (success) reportingPlayer.sendMessage(Text.translatable("gui.abuseReport.report_sent_msg", false));
        });
    }
}
