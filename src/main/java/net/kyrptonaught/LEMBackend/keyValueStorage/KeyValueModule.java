package net.kyrptonaught.LEMBackend.keyValueStorage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyrptonaught.LEMBackend.Module;

import java.util.concurrent.ConcurrentHashMap;

public class KeyValueModule extends Module {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> storage = new ConcurrentHashMap<>();

    public KeyValueModule() {
        super("kvs");
    }

    public String getValue(String id, String key) {
        return getIdStorage(id).get(key);
    }

    public JsonObject getValueAsJson(String id, String key) {
        return result(getValue(id, key));
    }

    public void setValue(String id, String key, String value) {
        getIdStorage(id).put(key, value);
    }

    public void resetValue(String id, String key) {
        getIdStorage(id).remove(key);
    }

    public ConcurrentHashMap<String, String> getIdStorage(String id) {
        if (!storage.containsKey(id))
            storage.put(id, new ConcurrentHashMap<>());

        return storage.get(id);
    }


    private JsonObject result(String value) {
        JsonObject result = new JsonObject();
        result.addProperty("value", value);
        return result;
    }

    @Override
    public void load(Gson gson) {
        createDirectories();

        JsonObject obj = readFileJson(gson, "kvs.json", JsonObject.class);
        if (obj == null) return;
        obj.entrySet().forEach(outer -> {
            String id = outer.getKey();
            JsonObject inner = outer.getValue().getAsJsonObject();
            inner.entrySet().forEach(innerElement -> {
                setValue(id, innerElement.getKey(), innerElement.getValue().getAsString());
            });
        });
    }

    @Override
    public void save(Gson gson) {
        createDirectories();
        writeFile("kvs.json", gson.toJson(storage));
    }
}
