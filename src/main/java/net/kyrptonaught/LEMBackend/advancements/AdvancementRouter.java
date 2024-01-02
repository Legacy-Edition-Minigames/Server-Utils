package net.kyrptonaught.LEMBackend.advancements;

import com.google.gson.JsonObject;
import io.javalin.http.Context;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.LEMBackend.ModuleRouter;
import net.minecraft.util.Identifier;

public class AdvancementRouter extends ModuleRouter<AdvancementModule> {

    @Override
    public void addRoutes() {
        route(HTTP.GET, "/v0/{secret}/getAdvancements/{uuid}", this::getAdvancements);
        route(HTTP.GET, "/v0/{secret}/unloadPlayer/{uuid}", this::unloadPlayer);
        route(HTTP.POST, "/v0/{secret}/addAdvancements/{uuid}", this::addAdvancement);
        route(HTTP.POST, "/v0/{secret}/removeAdvancements/{uuid}", this::removeAdvancement);
    }

    public void getAdvancements(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        JsonObject playerAdvancements = module.getAdvancementsFor(uuid);
        if (playerAdvancements != null) {
            ctx.json(playerAdvancements);
            return;
        }

        ctx.status(500).result("failed");
    }

    public void addAdvancement(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        JsonObject object = LEMBackend.gson.fromJson(ctx.body(), JsonObject.class);
        if (object != null) {
            module.grantAdvancement(uuid, new Identifier(object.get("advancementID").getAsString()), object.getAsJsonObject("advancement"), object.get("criterionName").getAsString());
            ctx.result("success");
            return;
        }

        ctx.status(500).result("failed");
    }

    public void removeAdvancement(Context ctx) {
        String uuid = ctx.pathParam("uuid");

        JsonObject object = LEMBackend.gson.fromJson(ctx.body(), JsonObject.class);
        if (object != null) {
            module.revokeAdvancement(uuid, new Identifier(object.get("advancementID").getAsString()), object.getAsJsonObject("advancement"), object.get("criterionName").getAsString());
            ctx.result("success");
            return;
        }

        ctx.status(500).result("failed");
    }

    //todo implement
    public void unloadPlayer(Context ctx) {
        String uuid = ctx.pathParam("uuid");
        ctx.result("success");
    }
}
