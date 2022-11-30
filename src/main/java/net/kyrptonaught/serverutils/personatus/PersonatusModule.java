package net.kyrptonaught.serverutils.personatus;

import com.google.gson.JsonObject;
import com.mojang.authlib.HttpAuthenticationService;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;

import java.io.IOException;
import java.net.URL;

public class PersonatusModule extends ModuleWConfig<PersonatusConfig> {

    public void onInitialize() {

    }


    public static String URLGetValue(HttpAuthenticationService service, String url, String key) throws IOException {
        String response = service.performGetRequest(new URL(url));

        if (response != null && !response.isEmpty()) {

            JsonObject obj = ServerUtilsMod.config.getGSON().fromJson(response, JsonObject.class);
            if (obj != null && obj.has(key))
                return obj.get(key).getAsString();
        }
        return null;
    }


    public static boolean isEnabled() {
        return ServerUtilsMod.personatusModule.getConfig().enabled;
    }

    @Override
    public PersonatusConfig createDefaultConfig() {
        return new PersonatusConfig();
    }
}
