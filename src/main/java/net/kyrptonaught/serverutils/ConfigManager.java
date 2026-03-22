package net.kyrptonaught.serverutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigManager {
    public static Path dir;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .create();

    public static void onInitialize() {
        dir = FabricLoader.getInstance().getConfigDir().resolve(ServerUtilsMod.ID);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {
            }
        }
    }

    public static Gson getGSON() {
        return GSON;
    }

    public static void save(String MOD_ID, AbstractConfigFile config) {
        Path saveFile = dir.resolve(MOD_ID + ".json5");
        try (OutputStream os = Files.newOutputStream(saveFile); OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            out.write(GSON.toJson(config));
        } catch (Exception e) {
            System.out.println(getConfigName(MOD_ID, "Failed to save config"));
            e.printStackTrace();
        }
    }

    public static AbstractConfigFile load(String MOD_ID, AbstractConfigFile defaultConfig) {
        Path saveFile = dir.resolve(MOD_ID + ".json5");
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

    private static String getConfigName(String MOD_ID, String message) {
        return "[" + MOD_ID + "]: " + message;
    }

    public static <T> T readFileJson(String file, Class<T> clazz) {
        Path saveFile = FabricLoader.getInstance().getConfigDir().resolve("serverutils").resolve(file);
        if (Files.exists(saveFile) && Files.isReadable(saveFile)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(saveFile, StandardOpenOption.READ), StandardCharsets.UTF_8)) {
                return GSON.fromJson(reader, clazz);
            } catch (Exception e) {
                System.out.println("Error opening file: " + saveFile);
                e.printStackTrace();
            }
        }
        return null;
    }
}
