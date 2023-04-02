package net.kyrptonaught.serverutils.userConfig;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class UserConfigStorage {
    private static final HashMap<UUID, DataStorage> players = new HashMap<>();

    public static void setValue(ServerPlayerEntity player, Identifier key, Object value) {
        if (!players.containsKey(player.getUuid())) players.put(player.getUuid(), new DataStorage());

        players.get(player.getUuid()).setValue(null,key, value.toString());
    }

    public static String getValue(ServerPlayerEntity player, Identifier key) {
        if (!players.containsKey(player.getUuid())) players.put(player.getUuid(), new DataStorage());

        return players.get(player.getUuid()).getValue(null, key);
    }

    private static class DataStorage {
        private static final HashMap<String, HashMap<Identifier, String>> values = new HashMap<>();

        public void setValue(DataType<?> dataType, Identifier key, String value) {
            if (!values.containsKey(dataType.cmdName)) values.put(dataType.cmdName, new HashMap<>());
            values.get(dataType.cmdName).put(key, value);
        }

        public String getValue(DataType<?> dataType, Identifier key) {
            if (values.containsKey(dataType.cmdName))
                return values.get(dataType.cmdName).get(key);

            return null;
        }
    }
}
