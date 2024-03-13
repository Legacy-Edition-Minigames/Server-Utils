package net.kyrptonaught.serverutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.serverutils.customMapLoader.addons.ResourcePackList;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigManager {
    private final Path dir;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .registerTypeAdapter(ResourcePackList.class, new ResourcePackList.Deserializer())
            .create();

    public ConfigManager(String MOD_ID) {
        dir = Path.of(FabricLoader.getInstance().getConfigDir() + "/" + MOD_ID);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {
            }
        }
    }

    public Gson getGSON() {
        return GSON;
    }

    public Path getDir() {
        return dir;
    }

    public void save(String MOD_ID, AbstractConfigFile config) {
        Path saveFile = dir.resolve(MOD_ID + ".json");
        try (OutputStream os = Files.newOutputStream(saveFile); OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            out.write(GSON.toJson(config));
        } catch (Exception e) {
            System.out.println(getConfigName(MOD_ID, "Failed to save config"));
            e.printStackTrace();
        }
    }

    public AbstractConfigFile load(String MOD_ID, AbstractConfigFile defaultConfig) {
        Path saveFile = dir.resolve(MOD_ID + ".json");
        if (!Files.exists(saveFile) || !Files.isReadable(saveFile)) {
            System.out.println(getConfigName(MOD_ID, "Unable to find config!"));
            return defaultConfig;
        }

        try (InputStream in = Files.newInputStream(saveFile, StandardOpenOption.READ);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, defaultConfig.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(getConfigName(MOD_ID, "Failed to load config!"));
        return null;
    }

    private String getConfigName(String MOD_ID, String message) {
        return "[" + MOD_ID + "]: " + message;
    }
}
