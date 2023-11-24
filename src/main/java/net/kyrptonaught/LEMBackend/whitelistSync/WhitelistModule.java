package net.kyrptonaught.LEMBackend.whitelistSync;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kyrptonaught.LEMBackend.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WhitelistModule extends Module {
    public static final List<WhitelistEntry> whitelist = Collections.synchronizedList(new ArrayList<>());

    public WhitelistModule() {
        super("whitelist");
    }

    public void add(String uuid, String name) {
        for (WhitelistEntry entry : whitelist) {
            if (entry.name.equals(name) || entry.uuid.equals(uuid)) {
                return;
            }
        }
        whitelist.add(new WhitelistEntry(uuid, name));
        save();
    }

    public void remove(String uuid, String name) {
        for (int i = whitelist.size() - 1; i >= 0; i--) {
            WhitelistEntry entry = whitelist.get(i);
            if (entry.name.equals(name) || entry.uuid.equals(uuid)) {
                whitelist.remove(entry);
                save();
            }
        }
    }

    public WhitelistEntry[] getList() {
        return whitelist.toArray(WhitelistEntry[]::new);
    }

    public void clear() {
        whitelist.clear();
        save();
    }

    @Override
    public void load(Gson gson) {
        createDirectories();

        JsonArray obj = readFileJson(gson, "whitelist.json", JsonArray.class);
        if (obj == null) return;
        for (JsonElement entry : obj) {
            add(entry.getAsJsonObject().get("uuid").getAsString(), entry.getAsJsonObject().get("name").getAsString());
        }
    }

    @Override
    public void save(Gson gson) {
        createDirectories();
        writeFile("whitelist.json", gson.toJson(whitelist.toArray(WhitelistEntry[]::new)));
    }

    public static class WhitelistEntry {
        public String uuid;
        public String name;

        public WhitelistEntry(String uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }
}