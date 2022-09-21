package net.kyrptonaught.serverutils.advancementSync;

import net.minecraft.server.ServerAdvancementLoader;

public interface PATLoadFromString {

    void loadFromString(ServerAdvancementLoader advancementLoader, String json);
}
