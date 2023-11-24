package net.kyrptonaught.LEMBackend.linking;

import io.javalin.http.Context;
import net.kyrptonaught.LEMBackend.ModuleRouter;

public class LinkRouter extends ModuleRouter<LinkingModule> {

    @Override
    public void addRoutes() {
        route(HTTP.POST, "/v0/{secret}/link/start/{linkid}/{mcuuid}", this::startLink);
        route(HTTP.POST, "/v0/{secret}/link/finish/{linkid}/{discordid}", this::linkPlayer);
        route(HTTP.POST, "/v0/{secret}/link/sus/add/{mcuuid}", this::addSus);
        route(HTTP.POST, "/v0/{secret}/link/sus/remove/{mcuuid}", this::removeSus);
        route(HTTP.GET, "/v0/{secret}/link/sus/check/{mcuuid}", this::checkSus);
    }

    public void startLink(Context ctx) {
        String linkID = ctx.pathParam("linkid");
        String mcUUID = ctx.pathParam("mcuuid");

        module.startLink(linkID, mcUUID);
        ctx.result("success");
    }

    public void linkPlayer(Context ctx) {
        String linkID = ctx.pathParam("linkid");
        String discordID = ctx.pathParam("discordid");

        String mcUUID = module.finishLink(linkID, discordID);
        if (mcUUID != null) {
            ctx.result(mcUUID);
            return;
        }

        ctx.status(500).result("failed");
    }


    public void addSus(Context ctx) {
        String mcUUID = ctx.pathParam("mcuuid");

        module.addSus(mcUUID);
        ctx.result("success");
    }

    public void removeSus(Context ctx) {
        String mcUUID = ctx.pathParam("mcuuid");

        module.removeSus(mcUUID);
        ctx.result("success");
    }

    public void checkSus(Context ctx) {
        String mcUUID = ctx.pathParam("mcuuid");

        ctx.json(module.isSus(mcUUID));
    }
}
