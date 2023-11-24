package net.kyrptonaught.LEMBackend.advancements;

import com.google.gson.JsonObject;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.LEMBackend.Module;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
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
            Advancement advancement = buildAdvancement(advancementID, advancementJSON);
            tracker.grantCriterion(advancement, criterionName);
            tracker.save();
        });
    }

    public void revokeAdvancement(String uuid, Identifier advancementID, JsonObject advancementJSON, String criterionName) {
        LEMBackend.minecraftServer.execute(() -> {
            PlayerAdvancementTracker tracker = loadAdvancementTracker(uuid, LEMBackend.minecraftServer);
            Advancement advancement = buildAdvancement(advancementID, advancementJSON);
            tracker.revokeCriterion(advancement, criterionName);
            tracker.save();
        });
    }

    public Advancement buildAdvancement(Identifier advancementID, JsonObject advancementJSON) {
        Advancement.Builder builder = Advancement.Builder.fromJson(advancementJSON, new AdvancementEntityPredicateDeserializer(advancementID, LEMBackend.minecraftServer.getLootManager()));
        builder.findParent(identifier -> LEMBackend.minecraftServer.getAdvancementLoader().get(identifier));
        return builder.build(advancementID);
    }

    public PlayerAdvancementTracker loadAdvancementTracker(String uuid, MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(uuid + ".json");
        return new PlayerAdvancementTracker(server.getDataFixer(), server.getPlayerManager(), server.getAdvancementLoader(), path, null);
    }
}
