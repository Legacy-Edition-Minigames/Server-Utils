package net.kyrptonaught.LEMBackend.whitelistSync;

import io.javalin.http.Context;
import net.kyrptonaught.LEMBackend.ModuleRouter;

public class WhitelistRouter extends ModuleRouter<WhitelistModule> {

    @Override
    public void addRoutes() {
        route(HTTP.GET, "/v0/{secret}/whitelist/get", this::getWhitelist);
        route(HTTP.POST, "/v0/{secret}/whitelist/add/{uuid}/{mcname}", this::addWhitelist);
        route(HTTP.POST, "/v0/{secret}/whitelist/remove/{uuid}/{mcname}", this::removeWhitelist);
        route(HTTP.POST, "/v0/{secret}/whitelist/clear", this::clearWhitelist);
    }

    private void addWhitelist(Context ctx) {
        String uuid = ctx.pathParam("uuid");
        String name = ctx.pathParam("mcname");

        module.add(uuid, name);
        ctx.result("success");
    }

    private void removeWhitelist(Context ctx) {
        String uuid = ctx.pathParam("uuid");
        String name = ctx.pathParam("mcname");

        module.remove(uuid, name);
        ctx.result("success");
    }

    private void getWhitelist(Context ctx) {
        ctx.json(module.getList());
    }

    private void clearWhitelist(Context ctx) {
        module.clear();
        ctx.result("success");
    }
}