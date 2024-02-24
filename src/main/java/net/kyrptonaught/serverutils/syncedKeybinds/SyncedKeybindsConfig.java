package net.kyrptonaught.serverutils.syncedKeybinds;

import net.kyrptonaught.serverutils.AbstractConfigFile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class SyncedKeybindsConfig extends AbstractConfigFile {

    public HashMap<Identifier, KeybindConfigItem> keybinds = new HashMap<>();

    public static class KeybindConfigItem {
        public String triggerCMD;
        public String keybinding;
        public String controllerBind;

        public void writeToPacket(Identifier id, PacketByteBuf packetByteBuf) {
            packetByteBuf.writeIdentifier(id);
            packetByteBuf.writeString(keybinding);
            packetByteBuf.writeString(controllerBind);
        }
    }
}
