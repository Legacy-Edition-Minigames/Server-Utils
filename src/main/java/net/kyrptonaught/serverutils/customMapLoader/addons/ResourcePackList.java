package net.kyrptonaught.serverutils.customMapLoader.addons;

import com.google.gson.*;
import net.kyrptonaught.serverutils.switchableresourcepacks.ResourcePackConfig;
import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ResourcePackList {

    public List<ResourcePackConfig.RPOption> packs = new ArrayList<>();

    public static class Deserializer implements JsonDeserializer<ResourcePackList> {
        @Override
        public ResourcePackList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ResourcePackList packList = new ResourcePackList();
            JsonArray array = json.getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    packList.packs.add(context.deserialize(array.get(i), ResourcePackConfig.RPOption.class));
                } else {
                    packList.packs.add(SwitchableResourcepacksMod.rpOptionHashMap.get(array.get(i).getAsString()));
                }
            }

            return packList;
        }
    }
}