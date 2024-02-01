package net.kyrptonaught.serverutils.customMapLoader;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;

public class Votebook {

    public static HashMap<String, BookElementBuilder> bookLibrary = new HashMap<>();

    public static void generateBookLibrary(List<LemModConfig> lemmods) {
        bookLibrary.clear();
        bookLibrary.put("404", createBasicBook().addPage(
                Text.literal("404"),
                Text.literal("Page not found"),
                Text.empty(),
                backButton("title")
        ));

        bookLibrary.put("title", createBasicBook().addPage(
                Text.literal("Legacy Edition Battle").formatted(Formatting.BLUE),
                Text.translatable("lem.mapdecider.menu.header").formatted(Formatting.DARK_AQUA),
                Text.translatable("lem.mapdecider.menu.credit", withLink(Text.literal("DBTDerpbox & Kyrptonaught").styled(style -> style.withColor(0x99C1F1).withUnderline(true)), "https://www.legacyminigames.net/credits")).styled(style -> style.withColor(0x62A0EA)),
                Text.empty(),
                Text.translatable("lem.mapdecider.menu.supportplease", withLink(Text.literal("Patreon").styled(style -> style.withUnderline(true).withColor(0xFF424D)), "https://www.legacyminigames.net/patreon")).formatted(Formatting.GOLD),
                Text.empty(),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.mapvoting"), "mapVoting"),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.hosts"), "hostConfig")
        ));

        bookLibrary.put("mapVoting", createBasicBook().addPage(
                dashTrans("lem.mapdecider.menu.mapvoting"),
                Text.empty(),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.basegame"), "baseMaps"),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.mods"), "customMaps"),
                Text.empty(),
                withCmd(bracketTrans("lem.mapdecider.menu.removevote").formatted(Formatting.RED), "/custommaploader vote removeVote"),
                Text.empty(),
                backButton("title")
        ));

        bookLibrary.put("hostConfig", createBasicBook().addPage(
                dashTrans("lem.mapdecider.menu.hosts"),
                Text.empty(),
                withCmd(bracketTrans("lem.mapdecider.menu.gameoptions"), "/trigger lem.gamecfg"),
                withCmd(bracketTrans("lem.mapdecider.menu.transferhost"), "/trigger lem.gamecfg set 103"),
                withOpenCmd(bracketLit("Map Settings"), "host_map_settings"),
                Text.empty(),
                backButton("title")
        ));

        generateHostSettings();
        generateMapPacks(false, lemmods.stream().filter(config -> !config.isBaseMap).toList());
        generateMapPacks(true, lemmods.stream().filter(config -> config.isBaseMap).toList());
        generateMaps(lemmods);
    }

    private static BookElementBuilder createBasicBook() {
        return new BookElementBuilder()
                .setTitle("Voting Book")
                .setAuthor("LEM");
    }

    public static void openbook(ServerPlayerEntity player, String book) {
        BookElementBuilder bookElement = bookLibrary.get(book);

        if (bookElement == null) bookElement = bookLibrary.get("404");

        new BookGui(player, bookElement).open();
    }

    public static void giveBook(ServerPlayerEntity player, String book) {
        BookElementBuilder bookElement = bookLibrary.get(book);

        if (bookElement == null) bookElement = bookLibrary.get("404");

        player.giveItemStack(new BookGui(player, bookElement).getBook());
    }

    public static void generateMapPacks(boolean isBase, List<LemModConfig> lemmods) {
        HashMap<String, List<LemModConfig>> packs = new HashMap<>();

        for (LemModConfig config : lemmods) {
            String pack = config.mappack;

            if (!packs.containsKey(pack)) {
                packs.put(pack, new ArrayList<>());
            }

            packs.get(pack).add(config);
        }

        List<Text> packsText = new ArrayList<>();
        packsText.add(dashTrans("lem.mapdecider.menu.mapvoting"));
        packsText.add(Text.empty());

        for (String pack : packs.keySet()) {
            if (pack.equals("base_base")) continue;

            BookElementBuilder builder = createBasicBook();

            List<LemModConfig> packMods = packs.get(pack);
            List<List<Text>> packPages = generateMapPackPages(packMods, true);

            MutableText hover = Text.empty();

            for (int i = 0; i < 6 && i < packMods.size(); i++) {
                hover.append(packMods.get(i).getName());
                if (i != packMods.size() - 1) hover.append("\n");
                if (i == 5 && packMods.size() > 6) hover.append("...");
            }

            for (int i = 0; i < packPages.size(); i++) {
                packPages.get(i).add(Text.empty());

                if (packPages.size() == 1) {
                    packPages.get(i).add(backButton(isBase ? "baseMaps" : "customMaps"));
                    builder.addPage(packPages.get(i).toArray(Text[]::new));
                    continue;
                }

                if (i == packPages.size() - 1) {
                    packPages.get(i).add(nextButton("Back", i));
                } else if (i == 0) {
                    packPages.get(i).add(backButton(isBase ? "baseMaps" : "customMaps").append(" ").append(nextButton("Next", i + 2)));
                } else {
                    packPages.get(i).add(nextButton("Back", i).append(" ").append(nextButton("Next", i + 2)));
                }

                builder.addPage(packPages.get(i).toArray(Text[]::new));
            }

            packsText.add(withOpenCmd(bracket(packs.get(pack).get(0).getMapPack()).formatted(Formatting.GOLD), "mapPack_" + pack, hover));
            bookLibrary.put("mapPack_" + pack, builder);
        }

        if(isBase) {
            List<List<Text>> base_pages = generateMapPackPages(packs.get("base_base"), false);
            packsText.addAll(base_pages.get(0));
        }

        packsText.add(Text.empty());
        packsText.add(backButton("mapVoting"));
        bookLibrary.put(isBase ? "baseMaps" : "customMaps", createBasicBook().addPage(packsText.toArray(Text[]::new)));
    }

    public static List<List<Text>> generateMapPackPages(List<LemModConfig> packMods, boolean includeHeader) {
        int maxPerPage = 10;
        int lastUsed = 0;

        List<List<Text>> pages = new ArrayList<>();

        while (lastUsed < packMods.size()) {
            List<Text> pageText = new ArrayList<>();
            if(includeHeader) {
                pageText.add(dash(packMods.get(0).getMapPack()));
                pageText.add(Text.empty());
            }

            for (; lastUsed < packMods.size() && lastUsed - (pages.size() * maxPerPage) < maxPerPage; lastUsed++) {
                LemModConfig config = packMods.get(lastUsed);

                String availableSizes = "Available sizes: " +
                        (config.hassmall ? "Small, " : "") +
                        (config.haslarge ? "Large, " : "") +
                        (config.haslargeplus ? "Large+, " : "") +
                        (config.hasremastered ? "Remastered, " : "");

                Text tooltip = config.getName().append("\n")
                        .append("By: " + config.authors).append("\n")
                        .append("Version: " + config.version).append("\n")
                        .append("Resource Pack: " + config.pack).append("\n")
                        .append(availableSizes.substring(0, availableSizes.length() - 2));
                pageText.add(withHover(withOpenCmd(bracket(trimName(config.getName(), 20)), "map_" + config.id), tooltip));
            }
            pages.add(pageText);
        }

        return pages;
    }

    public static void generateMaps(List<LemModConfig> lemmods) {
        for (LemModConfig config : lemmods) {
            List<Text> mapText = new ArrayList<>();
            String availableSizes = "Available sizes: " +
                    (config.hassmall ? "Small, " : "") +
                    (config.haslarge ? "Large, " : "") +
                    (config.haslargeplus ? "Large+, " : "") +
                    (config.hasremastered ? "Remastered, " : "");

            Text tooltip = config.getName().append("\n")
                    .append("By: " + config.authors).append("\n")
                    .append("Version: " + config.version).append("\n")
                    .append("Resource Pack: " + config.pack).append("\n")
                    .append(availableSizes.substring(0, availableSizes.length() - 2));

            mapText.add(withHover(config.getName().formatted(Formatting.BLUE), tooltip));
            mapText.add(config.getDescription().formatted(Formatting.DARK_AQUA));
            mapText.add(Text.empty());

            mapText.add(voteButton(config.id).append(" ").append(backButton("mapPack_" + config.mappack)));

            bookLibrary.put("map_" + config.id, createBasicBook().addPage(mapText.toArray(Text[]::new)));
        }
    }

    public static void generateHostSettings() {
        Text tooltip = Text.literal("Choose between Small and Large maps or let the system decide automatically.").append("\n\n")
                .append("Large+ maps are like Large maps but come with an extra helping of chests!").append("\n\n")
                .append("Note: Playing on a Small or Large map with more than 8 players will automatically disable the Central Spawn option!");

        MapSize selected = HostOptions.selectedMapSize;

        bookLibrary.put("host_map_settings", createBasicBook().addPage(
                dashLit("Map Settings"),
                Text.empty(),
                withOpenCmd(bracketTrans("lem.menu.host.config.maps.enabled.header"), "host_map_enabled"),
                Text.empty(),
                withHover(colored("Selected Map Size:", Formatting.GOLD), tooltip),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.auto"), selected == MapSize.AUTO ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Auto"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.small"), selected == MapSize.SMALL ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Small"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.large"), selected == MapSize.LARGE ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Large"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.largeplus"), selected == MapSize.LARGE_PLUS ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Large+"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.remastered"), selected == MapSize.REMASTERED ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Remastered"),
                Text.empty(),
                backButton("hostConfig")
        ));
    }

    public static MutableText trimName(MutableText text, int maxLength) {
        if (text.getContent() instanceof PlainTextContent content) {
            String value = content.string();

            if (value.length() > maxLength) {
                value = value.substring(0, maxLength - 3) + "...";
            }

            return Text.literal(value);
        }

        return text;
    }

    public static MutableText colored(MutableText text, Formatting... formatting) {
        return text.formatted(formatting);
    }

    public static MutableText colored(String text, Formatting... formatting) {
        return colored(Text.literal(text), formatting);
    }

    public static MutableText bracketTrans(String transKey) {
        return bracket(Text.translatable(transKey));
    }

    public static MutableText bracketLit(String text) {
        return bracket(Text.literal(text));
    }

    public static MutableText bracket(MutableText text) {
        return colored(Text.literal("[").append(text).append("]"), Formatting.GOLD);
    }

    public static MutableText dashTrans(String transKey) {
        return dash(Text.translatable(transKey));
    }

    public static MutableText dashLit(String text) {
        return dash(Text.literal(text));
    }

    public static MutableText dash(MutableText text) {
        return colored(Text.literal("- ").append(text).append(" -"), Formatting.BLUE);
    }

    public static MutableText voteButton(String map) {
        return bracketLit("Vote").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/custommaploader voting vote " + map)));
    }

    public static MutableText backButton(String page) {
        return colored(withOpenCmd(bracketLit("Back"), page), Formatting.GRAY);
    }

    public static MutableText nextButton(String text, int page) {
        return colored(withClickEvent(bracketLit(text), ClickEvent.Action.CHANGE_PAGE, "" + page), Formatting.GRAY);
    }

    public static MutableText withHover(MutableText text, Text hover) {
        return text.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
    }

    public static MutableText withOpenCmd(MutableText text, String page) {
        return withCmd(text, "/custommaploader voting showBookPage " + page);
    }

    public static MutableText withOpenCmd(MutableText text, String page, Text hover) {
        return withHover(withOpenCmd(text, page), hover);
    }

    public static MutableText withOpenAfterCmd(MutableText text, String page, String afterCmd) {
        return withCmd(text, "/custommaploader voting showBookPage " + page + " after " + afterCmd);
    }

    public static MutableText withCmd(MutableText text, String cmd) {
        return withClickEvent(text, ClickEvent.Action.RUN_COMMAND, cmd);
    }

    public static MutableText withClickEvent(MutableText text, ClickEvent.Action action, String option) {
        return text.styled(style -> style.withClickEvent(new ClickEvent(action, option)));
    }

    public static MutableText withLink(MutableText text, String url) {
        return text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(url))));
    }
}
