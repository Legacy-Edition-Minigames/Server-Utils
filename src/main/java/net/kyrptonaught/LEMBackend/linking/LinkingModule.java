package net.kyrptonaught.LEMBackend.linking;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.LEMBackend.Module;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LinkingModule extends Module {

    private final List<Link> links = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<String, Link> mcToLinks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Link> discordToLinks = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, LinkInProgress> linksInProgress = new ConcurrentHashMap<>();

    private final HashSet<String> sussies = new HashSet<>();

    public LinkingModule() {
        super("links");
    }

    public void startLink(String linkID, String mcUUID) {
        linksInProgress.put(linkID, new LinkInProgress(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), mcUUID));
    }

    public String finishLink(String linkID, String discordID) {
        LinkInProgress link = linksInProgress.remove(linkID);

        if (link != null) {
            addLink(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), link.mcUUID, discordID);
            saveLinks();
            return link.mcUUID;
        }
        return null;
    }

    public void addLink(String dateLinked, String mcUUID, String discordID) {
        if (mcToLinks.containsKey(mcUUID) || discordToLinks.containsKey(discordID))
            return;
        Link link = new Link(dateLinked, mcUUID, discordID);
        links.add(link);
        mcToLinks.put(mcUUID, link);
        discordToLinks.put(discordID, link);
    }

    public void addSus(String mcUUID) {
        sussies.add(mcUUID);
        saveSus();
    }

    public boolean isSus(String mcUUID) {
        return sussies.contains(mcUUID);
    }

    public void removeSus(String mcUUID) {
        sussies.remove(mcUUID);
        saveSus();
    }

    @Override
    public void load(Gson gson) {
        createDirectories();

        JsonArray linkJson = readFileJson(gson, "links.json", JsonArray.class);
        if (linkJson != null)
            for (JsonElement item : linkJson) {
                JsonObject link = item.getAsJsonObject();
                addLink(link.get("dateLinked").getAsString(), link.get("mcUUID").getAsString(), link.get("discordID").getAsString());
            }

        JsonArray susJson = readFileJson(gson, "sus.json", JsonArray.class);
        if (linkJson != null)
            for (JsonElement item : susJson) {
                sussies.add(item.getAsString());
            }
    }

    @Override
    public void save(Gson gson) {
        createDirectories();
        saveLinks();
        saveSus();
    }

    public void saveLinks() {
        writeFile("links.json", LEMBackend.gson.toJson(links));
    }

    public void saveSus() {
        writeFile("sus.json", LEMBackend.gson.toJson(sussies));
    }

    public static class Link {
        private final String dateLinked;
        private final String mcUUID;
        private final String discordID;

        public Link(String dateLinked, String mcUUID, String discordID) {
            this.dateLinked = dateLinked;
            this.mcUUID = mcUUID;
            this.discordID = discordID;
        }
    }

    public static class LinkInProgress {
        private final String linkStarted;
        private final String mcUUID;

        public LinkInProgress(String startTime, String mcUUID) {
            this.linkStarted = startTime;
            this.mcUUID = mcUUID;
        }
    }
}
