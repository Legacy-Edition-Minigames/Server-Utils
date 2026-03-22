package net.kyrptonaught.serverutils.backendLink;

import net.kyrptonaught.serverutils.AbstractConfigFile;

public class BackendServerConfig extends AbstractConfigFile {

    public String apiUrl = "http://localhost:7070";
    public String secretKey = "changeme";

    public String serverName = "Test Server";
    public String bridgeName = "test-server";
}
