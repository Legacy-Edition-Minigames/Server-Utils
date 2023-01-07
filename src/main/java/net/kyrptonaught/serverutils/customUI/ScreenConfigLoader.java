package net.kyrptonaught.serverutils.customUI;

import net.kyrptonaught.serverutils.FileHelper;
import net.kyrptonaught.serverutils.ServerUtilsMod;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class ScreenConfigLoader {

    public static HashMap<String, ScreenConfig> loadAll() {
        HashMap<String, ScreenConfig> screenConfigs = new HashMap<>();

        Path path = ServerUtilsMod.config.getDir().resolve(ServerUtilsMod.CustomUIModule.getMOD_ID());
        FileHelper.createDir(path);

        try (Stream<Path> stream = Files.walk(path)) {
            stream.forEach(file -> {
                if (file.getFileName().toString().endsWith(".json") || file.getFileName().toString().endsWith(".json5"))
                    load(file).ifPresent(screenConfig -> screenConfigs.put(getRawFileName(file.getFileName().toString()), screenConfig));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return screenConfigs;
    }

    private static String getRawFileName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private static Optional<ScreenConfig> load(Path path) {
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ);
             InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(ServerUtilsMod.getGson().fromJson(reader, ScreenConfig.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}