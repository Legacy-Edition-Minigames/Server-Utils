package net.kyrptonaught.LEMBackend;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Module {

    protected final Path savePath;

    public Module() {
        this.savePath = LEMBackend.getBaseConfigPath();
    }

    public Module(String savePath) {
        this.savePath = LEMBackend.getBaseConfigPath().resolve(savePath);
    }

    protected void save() {
        save(LEMBackend.gson);
    }

    protected void load() {
        load(LEMBackend.gson);
    }

    public void save(Gson gson) {

    }

    public void load(Gson gson) {

    }

    public <T> T readFileJson(Gson gson, String file, Class<T> clazz) {
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

    public void writeFile(String file, String data) {
        Path saveFile = savePath.resolve(file);
        try (OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(saveFile), StandardCharsets.UTF_8)) {
            out.write(data);
        } catch (Exception e) {
            System.out.println("Error writing file: " + saveFile);
            e.printStackTrace();
        }
    }

    protected void createDirectories() {
        try {
            Files.createDirectories(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
