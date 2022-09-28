package net.kyrptonaught.serverutils.advancementSync;

import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

public class AdvancementSyncConfig implements AbstractConfigFile {

    public String apiUrl = "http://localhost:7070";
    public String secretKey = "changeme";

    public boolean syncOnJoin = true;


    public String getApiURL() {
        return apiUrl + "/v0/" + secretKey;
    }
}
