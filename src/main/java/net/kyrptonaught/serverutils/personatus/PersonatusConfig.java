package net.kyrptonaught.serverutils.personatus;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class PersonatusConfig extends AbstractConfigFile {

    public String apiUrl = "http://localhost:7070";
    public String secretKey = "changeme";

    public boolean enabled = true;


    public String getApiURL() {
        return apiUrl + "/v0/" + secretKey;
    }
}
