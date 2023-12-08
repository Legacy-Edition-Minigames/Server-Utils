package net.kyrptonaught.LEMBackend.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.LEMBackend.Module;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class AdvancementModule extends Module {

    public JsonObject getAdvancementsFor(String player) {
        Path path = LEMBackend.minecraftServer.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(player + ".json");
        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ), StandardCharsets.UTF_8)) {
            return LEMBackend.gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            System.out.println("Error opening file: " + path);
            e.printStackTrace();
        }
        return null;
    }

    public void grantAdvancement(String uuid, Identifier advancementID, JsonObject advancementJSON, String criterionName) {
        System.out.println(advancementJSON);
        LEMBackend.minecraftServer.execute(() -> {
            PlayerAdvancementTracker tracker = loadAdvancementTracker(uuid, LEMBackend.minecraftServer);
            AdvancementEntry advancement = buildAdvancement(advancementID, advancementJSON);
            tracker.grantCriterion(advancement, criterionName);
            tracker.save();
        });
    }

    public void revokeAdvancement(String uuid, Identifier advancementID, JsonObject advancementJSON, String criterionName) {
        LEMBackend.minecraftServer.execute(() -> {
            PlayerAdvancementTracker tracker = loadAdvancementTracker(uuid, LEMBackend.minecraftServer);
            AdvancementEntry advancement = buildAdvancement(advancementID, advancementJSON);
            tracker.revokeCriterion(advancement, criterionName);
            tracker.save();
        });
    }

    public AdvancementEntry buildAdvancement(Identifier advancementID, JsonObject advancementJSON) {
        Advancement advancement = Util.getResult(Advancement.CODEC.parse(JsonOps.INSTANCE, advancementJSON), JsonParseException::new);
        return new AdvancementEntry(advancementID, advancement);
    }

    public PlayerAdvancementTracker loadAdvancementTracker(String uuid, MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(uuid + ".json");
        return new PlayerAdvancementTracker(server.getDataFixer(), server.getPlayerManager(), server.getAdvancementLoader(), path, null);
    }
}
