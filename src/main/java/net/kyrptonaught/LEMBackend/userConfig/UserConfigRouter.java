package net.kyrptonaught.LEMBackend.userConfig;

import io.javalin.http.Context;
import net.kyrptonaught.LEMBackend.ModuleRouter;

public class UserConfigRouter extends ModuleRouter<UserConfigModule> {

    @Override
    public void addRoutes() {
        route(HTTP.GET, "/v0/{secret}/getUserConfig/{uuid}", this::getUserConfig);
        route(HTTP.POST, "/v0/{secret}/syncUserConfig/{uuid}", this::syncUserConfig);
    }

    public void getUserConfig(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        ctx.json(module.loadPlayer(uuid));
    }

    public void syncUserConfig(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        module.syncPlayer(uuid, ctx.body());
        ctx.result("success");
    }
}
