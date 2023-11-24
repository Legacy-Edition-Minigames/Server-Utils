package net.kyrptonaught.LEMBackend.userConfig;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.LEMBackend.Module;

public class UserConfigModule extends Module {

    public UserConfigModule() {
        super("userConfigs");
    }

    public JsonObject loadPlayer(String player) {
        JsonObject obj = readFileJson(LEMBackend.gson, player + ".json", JsonObject.class);
        if (obj == null) obj = new JsonObject();

        return obj;
    }

    public void syncPlayer(String player, String json) {
        writeFile(player + ".json", json);
    }

    @Override
    public void load(Gson gson) {
        createDirectories();
    }
}