package net.kyrptonaught.serverutils.userConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.serverutils.FileHelper;
import net.kyrptonaught.serverutils.ServerUtilsMod;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class UserConfigLocalStorage {
    private static Path savePath = FabricLoader.getInstance().getGameDir().resolve("userConfigs");

    public static JsonObject loadPlayer(String player) {
        JsonObject obj = readFileJson(ServerUtilsMod.getGson(), player + ".json", JsonObject.class);
        if (obj == null) obj = new JsonObject();

        return obj;
    }

    public static void syncPlayer(String player, String json) {
        FileHelper.createDir(savePath);
        writeFile(player + ".json", json);
    }

    public static <T> T readFileJson(Gson gson, String file, Class<T> clazz) {
        Path saveFile = savePath.resolve(file);
        if (Files.exists(saveFile) && Files.isReadable(saveFile)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(saveFile, StandardOpenOption.READ), StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, clazz);
            } catch (Exception e) {
                System.out.println("Error opening file: " + saveFile);
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void writeFile(String file, String data) {
        Path saveFile = savePath.resolve(file);
        try (OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(saveFile), StandardCharsets.UTF_8)) {
            out.write(data);
        } catch (Exception e) {
            System.out.println("Error writing file: " + saveFile);
            e.printStackTrace();
        }
    }
}
