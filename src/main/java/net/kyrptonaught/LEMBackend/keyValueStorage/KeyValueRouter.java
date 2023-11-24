package net.kyrptonaught.LEMBackend.keyValueStorage;

import io.javalin.http.Context;
import net.kyrptonaught.LEMBackend.ModuleRouter;

public class KeyValueRouter extends ModuleRouter<KeyValueModule> {
    @Override
    public void addRoutes() {
        route(HTTP.GET, "/v0/{secret}/kvs/set/{id}/{key}/{value}", this::setValue);
        route(HTTP.GET, "/v0/{secret}/kvs/get/{id}/{key}", this::getValue);
        route(HTTP.GET, "/v0/{secret}/kvs/reset/{id}/{key}", this::resetValue);
    }

    public void getValue(Context ctx) {
        String id = ctx.pathParam("id");
        String key = ctx.pathParam("key");

        ctx.json(module.getValueAsJson(id, key));
    }

    public void setValue(Context ctx) {
        String id = ctx.pathParam("id");
        String key = ctx.pathParam("key");
        String value = ctx.pathParam("value");

        module.setValue(id, key, value);
        ctx.result("success");
    }

    public void resetValue(Context ctx) {
        String id = ctx.pathParam("id");
        String key = ctx.pathParam("key");

        module.resetValue(id, key);
        ctx.result("success");
    }
}
