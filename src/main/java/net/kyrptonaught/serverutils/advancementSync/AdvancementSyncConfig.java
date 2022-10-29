package net.kyrptonaught.serverutils.advancementSync;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class AdvancementSyncConfig extends AbstractConfigFile {

    public String apiUrl = "http://localhost:7070";
    public String secretKey = "changeme";

    public boolean syncOnJoin = true;


    public String getApiURL() {
        return apiUrl + "/v0/" + secretKey;
    }
}
