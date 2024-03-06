package net.kyrptonaught.serverutils.customMapLoader.addons;

public class LobbyMapAddon extends BaseMapAddon {
    public static final String TYPE = "lobby_map";

    public String[] spawn_coords;

    public String winner_coords;

    public String getDirectoryInZip() {
        return "world/lobby/";
    }
}
