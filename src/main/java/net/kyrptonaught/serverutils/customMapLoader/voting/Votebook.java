package net.kyrptonaught.serverutils.customMapLoader.voting;

import eu.pb4.sgui.api.gui.BookGui;
import net.kyrptonaught.serverutils.customMapLoader.CustomMapLoaderMod;
import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.customMapLoader.voting.pages.BookPage;
import net.kyrptonaught.serverutils.customMapLoader.voting.pages.DynamicData;
import net.kyrptonaught.serverutils.switchableresourcepacks.ResourcePackConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class Votebook {

    public static HashMap<String, BookPage> bookLibrary = new HashMap<>();
    public static HashMap<String, BiConsumer<DynamicData,BookPage>> dynamicLibrary = new HashMap<>();

    public static void generateBookLibrary(List<BattleMapAddon> addons) {
        bookLibrary.clear();
        dynamicLibrary.clear();

        bookLibrary.put("title", createBasicPage().addPage(
                Text.literal("Legacy Edition Battle").formatted(Formatting.BLUE),
                Text.translatable("lem.mapdecider.menu.header").formatted(Formatting.DARK_AQUA),
                Text.translatable("lem.mapdecider.menu.credit", withLink(Text.literal("DBTDerpbox & Kyrptonaught").styled(style -> style.withColor(0x99C1F1).withUnderline(true)), "https://www.legacyminigames.net/credits")).styled(style -> style.withColor(0x62A0EA)),
                Text.empty(),
                Text.translatable("lem.mapdecider.menu.supportplease", withLink(Text.literal("Patreon").styled(style -> style.withUnderline(true).withColor(0xFF424D)), "https://www.legacyminigames.net/patreon")).formatted(Formatting.GOLD),
                Text.empty(),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.mapvoting"), "mapVoting"),
                withOpenCmd(bracketTrans("lem.generic.options"), "options")
        ));

        bookLibrary.put("mapVoting", createBasicPage().addPage(
                dashTrans("lem.mapdecider.menu.mapvoting"),
                Text.empty(),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.basegame"), "baseMaps"),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.mods"), "customMaps"),
                Text.empty(),
                withCmd(bracketTrans("lem.mapdecider.menu.removevote").formatted(Formatting.RED), "/custommaploader vote removeVote"),
                Text.empty(),
                backButton("title")
        ));

        bookLibrary.put("options", createBasicPage().addPage(
                dashTrans("lem.generic.options"),
                Text.empty(),
                withOpenCmd(bracketLit("Global Pack Policy"), "player_rp_settings"),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.hosts"), "hostConfig"),
                Text.empty(),
                backButton("title")
        ));

        bookLibrary.put("hostConfig", createBasicPage().addPage(
                dashTrans("lem.mapdecider.menu.hosts"),
                Text.empty(),
                withCmd(bracketTrans("lem.mapdecider.menu.gameoptions"), "/trigger lem.gamecfg"),
                withCmd(bracketTrans("lem.mapdecider.menu.transferhost"), "/trigger lem.gamecfg set 103"),
                withOpenCmd(bracketTrans("lem.mapdecider.menu.mapsettings"), "host_map_settings"),
                Text.empty(),
                backButton("options")
        ));

        dynamicLibrary.put("host_map_settings", Votebook::generateHostSettings);
        dynamicLibrary.put("host_map_enable_disable", Votebook::generateMapEnableDisable);
        dynamicLibrary.put("player_rp_settings", Votebook::generatePlayerPackPolicy);
        dynamicLibrary.put("map_player_rp_settings", Votebook::generateMapOptionalPacks);

        generateMapPacks(false, addons.stream().filter(config -> !config.isBaseAddon).toList());
        generateMapPacks(true, addons.stream().filter(config -> config.isBaseAddon).toList());
        generateMapPages(addons);
    }

    private static BookPage createBasicPage() {
        return new BookPage("Voting Book", "LEM");
    }

    public static BookGui getPage(ServerPlayerEntity player, String page, String arg) {
        if (dynamicLibrary.containsKey(page)) {
            BookPage book = createBasicPage();
            dynamicLibrary.get(page).accept(new DynamicData(player, CustomMapLoaderMod.BATTLE_MAPS.values().stream().toList(), arg), book);
            return book.build(player);
        }

        BookPage book = bookLibrary.get(page);
        if (book == null) book = createBasicPage().addPage(
                Text.translatable("lem.mapdecider.menu.missing"),
                Text.literal(page),
                Text.empty(),
                backButton("title")
        );
        return book.build(player);
    }

    public static void generateMapPacks(boolean isBase, List<BattleMapAddon> addons) {
        HashMap<String, List<BattleMapAddon>> packs = new HashMap<>();

        for (BattleMapAddon config : addons) {
            String pack = config.addon_pack;

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

            BookPage builder = createBasicPage();

            List<BattleMapAddon> packAddons = packs.get(pack);
            List<List<Text>> packPages = generateMapPackPages(packAddons, true);

            MutableText hover = Text.empty();

            for (int i = 0; i < 6 && i < packAddons.size(); i++) {
                hover.append(packAddons.get(i).getNameText());
                if (i != packAddons.size() - 1) hover.append("\n");
                if (i == 5 && packAddons.size() > 6) hover.append("...");
            }

            for (int i = 0; i < packPages.size(); i++) {
                packPages.get(i).add(Text.empty());

                if (packPages.size() == 1) {
                    packPages.get(i).add(backButton(isBase ? "baseMaps" : "customMaps"));
                    builder.addPage(packPages.get(i).toArray(Text[]::new));
                    continue;
                }

                if (i == packPages.size() - 1) {
                    packPages.get(i).add(nextButton("gui.back", i));
                } else if (i == 0) {
                    packPages.get(i).add(backButton(isBase ? "baseMaps" : "customMaps").append(" ").append(nextButton("createWorld.customize.custom.next", i + 2)));
                } else {
                    packPages.get(i).add(nextButton("gui.back", i).append(" ").append(nextButton("createWorld.customize.custom.next", i + 2)));
                }

                builder.addPage(packPages.get(i).toArray(Text[]::new));
            }

            packsText.add(withOpenCmd(bracket(packs.get(pack).get(0).getAddonPackText()).formatted(Formatting.GOLD), "mapPack_" + pack, hover));
            bookLibrary.put("mapPack_" + pack, builder);
        }

        if (isBase && packs.containsKey("base_base")) {
            List<List<Text>> base_pages = generateMapPackPages(packs.get("base_base"), false);
            packsText.addAll(base_pages.get(0));
        }

        packsText.add(Text.empty());
        packsText.add(backButton("mapVoting"));
        bookLibrary.put(isBase ? "baseMaps" : "customMaps", createBasicPage().addPage(packsText.toArray(Text[]::new)));
    }

    public static List<List<Text>> generateMapPackPages(List<BattleMapAddon> packMods, boolean includeHeader) {
        int maxPerPage = 10;
        int lastUsed = 0;

        List<List<Text>> pages = new ArrayList<>();

        while (lastUsed < packMods.size()) {
            List<Text> pageText = new ArrayList<>();
            if (includeHeader) {
                pageText.add(dash(packMods.get(0).getAddonPackText()));
                pageText.add(Text.empty());
            }

            for (; lastUsed < packMods.size() && lastUsed - (pages.size() * maxPerPage) < maxPerPage; lastUsed++) {
                BattleMapAddon config = packMods.get(lastUsed);

                pageText.add(withHover(withOpenCmd(bracket(trimName(config.getNameText(), 20)), "map_" + config.addon_id), generateMapTooltip(config)));
            }
            pages.add(pageText);
        }

        return pages;
    }

    public static void generateMapPages(List<BattleMapAddon> lemmods) {
        for (BattleMapAddon config : lemmods) {
            List<Text> mapText = new ArrayList<>();

            mapText.add(withHover(config.getNameText().formatted(Formatting.BLUE), generateMapTooltip(config)));
            mapText.add(config.getDescriptionText().formatted(Formatting.DARK_AQUA));
            mapText.add(Text.empty());

            mapText.add(voteButton(config.addon_id).append(" ").append(backButton("mapPack_" + config.addon_pack).append(" ").append(withOpenCmd(bracketLit("More"), "map_player_rp_settings", config.addon_id.toString()))));

            bookLibrary.put("map_" + config.addon_id, createBasicPage().addPage(mapText.toArray(Text[]::new)));
        }
    }

    public static Text generateMapTooltip(BattleMapAddon config) {
        MutableText availableTypes = Text.empty();
        if (config.hasSize(MapSize.SMALL))
            availableTypes.append(Text.translatable("lem.battle.menu.host.config.maps.option.small")).append(", ");
        if (config.hasSize(MapSize.LARGE))
            availableTypes.append(Text.translatable("lem.battle.menu.host.config.maps.option.large")).append(", ");
        if (config.hasSize(MapSize.LARGE_PLUS))
            availableTypes.append(Text.translatable("lem.battle.menu.host.config.maps.option.largeplus")).append(", ");
        if (config.hasSize(MapSize.REMASTERED))
            availableTypes.append(Text.translatable("lem.battle.menu.host.config.maps.option.remastered")).append(", ");
        availableTypes.getSiblings().remove(availableTypes.getSiblings().size() - 1);

        return config.getNameText().append("\n")
                .append(Text.translatable("mco.template.select.narrate.authors", config.authors)).append("\n")
                .append(Text.translatable("mco.version", config.version)).append("\n")
                //.append(Text.translatable("lem.mapdecider.menu.voting.pack", Text.translatable("lem.resource." + config.resource_pack + ".name"))).append("\n")
                .append(Text.translatable("lem.mapdecider.menu.voting.typelist", availableTypes));
    }

    public static void generateHostSettings(DynamicData data, BookPage bookPage) {
        MapSize selected = HostOptions.selectedMapSize;

        bookPage.addPage(
                dashTrans("lem.mapdecider.menu.mapsettings"),
                Text.empty(),
                withOpenCmd(withHover(bracketTrans("lem.menu.host.config.maps.enabled.header"), Text.translatable("lem.menu.host.config.maps.enabled.tooltip")), "host_map_enable_disable"),
                Text.empty(),
                withHover(coloredTrans("lem.battle.menu.host.config.maps.option.selectedsize", Formatting.GOLD), Text.translatable("lem.battle.menu.host.config.maps.option.selectedsize.tooltip")),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.auto"), selected == MapSize.AUTO ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Auto"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.small"), selected == MapSize.SMALL ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Small"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.large"), selected == MapSize.LARGE ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Large"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.largeplus"), selected == MapSize.LARGE_PLUS ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Large+"),
                withOpenAfterCmd(colored(bracketTrans("lem.battle.menu.host.config.maps.option.remastered"), selected == MapSize.REMASTERED ? Formatting.GREEN : Formatting.BLUE), "host_map_settings", "custommaploader hostOptions mapSize set Remastered"),
                Text.empty(),
                backButton("hostConfig"));
    }

    public static void generateMapEnableDisable(DynamicData data, BookPage bookPage) {
        List<BattleMapAddon> packMods = data.addons();
        int maxPerPage = 10;
        int lastUsed = 0;
        int pages = 0;

        while (lastUsed < packMods.size()) {
            List<Text> pageText = new ArrayList<>();

            pageText.add(dashTrans("lem.menu.host.config.maps.enabled.header"));
            pageText.add(Text.empty());

            for (; lastUsed < packMods.size() && lastUsed - (pages * maxPerPage) < maxPerPage; lastUsed++) {

                BattleMapAddon config = packMods.get(lastUsed);
                boolean enabled = config.isAddonEnabled;

                String cmd = "custommaploader hostOptions enableMap " + config.addon_id + " " + (!enabled);
                pageText.add(withHover(withOpenAfterCmd(colored(bracket(trimName(config.getNameText(), 20)), enabled ? Formatting.GREEN : Formatting.RED), "host_map_enable_disable", cmd), Text.translatable("lem.menu.host.config.maps.enabled.toggle")));
            }

            pageText.add(Text.empty());
            if (pages == 0) pageText.add(backButton("host_map_settings"));
            else pageText.add(nextButton("gui.back", pages));

            if (lastUsed < packMods.size())
                ((MutableText) pageText.get(pageText.size() - 1)).append(" ").append(nextButton("createWorld.customize.custom.next", pages + 2));

            bookPage.addPage(pageText.toArray(Text[]::new));
            pages++;
        }
    }

    public static void generateMapOptionalPacks(DynamicData data, BookPage bookPage) {
        BattleMapAddon addon = CustomMapLoaderMod.BATTLE_MAPS.get(new Identifier(data.arg()));

        int maxPerPage = 8;
        int lastUsed = 0;
        int pages = 0;

        while (lastUsed < addon.optional_packs.packs.size()) {
            List<Text> pageText = new ArrayList<>();

            pageText.add(withHover(dashLit("Optional Packs"), trimName(addon.getNameText(), 20)));
            pageText.add(Text.empty());

            for (; lastUsed < addon.optional_packs.packs.size() && lastUsed - (pages * maxPerPage) < maxPerPage; lastUsed++) {
                ResourcePackConfig.RPOption rpOption = addon.optional_packs.packs.get(lastUsed);

                boolean overwrite = HostOptions.getOverwriteValue(data.player(), addon.addon_id);

                if (lastUsed == 0) {
                    String cmd = "custommaploader playerOptions optionalPacks globalAccept overwrite " + addon.addon_id;
                    MutableText enabled = toggleCheckBox(overwrite, "map_player_rp_settings", addon.addon_id.toString(), cmd);
                    pageText.add(colored("Overwrite Global: ", Formatting.DARK_GRAY).append(enabled));
                    pageText.add(Text.empty());
                }

                if (overwrite) {
                    MutableText hover = rpOption.getNameText().append("\n").append(rpOption.getDescriptionText());
                    String cmd = "custommaploader playerOptions optionalPacks " + addon.addon_id + " accept " + rpOption.packID;
                    MutableText enabled = toggleCheckBox(HostOptions.getMapResourcePackValue(data.player(), addon.addon_id, rpOption.packID), "map_player_rp_settings", addon.addon_id.toString(), cmd).append(" ");
                    pageText.add(enabled.append(colored(withHover(trimName(rpOption.getNameText(), 20), hover), Formatting.DARK_GRAY)));
                } else {
                    MutableText hover = Text.literal("Enable overwrite to modify");
                    pageText.add(colored(withHover(toggleCheckBox(false).append(" ").append(trimName(rpOption.getNameText(), 20)), hover), Formatting.GRAY));
                }
            }

            pageText.add(Text.empty());
            if (pages == 0) pageText.add(backButton("map_" + addon.addon_id));
            else pageText.add(nextButton("gui.back", pages));

            if (lastUsed < addon.optional_packs.packs.size())
                ((MutableText) pageText.get(pageText.size() - 1)).append(" ").append(nextButton("createWorld.customize.custom.next", pages + 2));

            bookPage.addPage(pageText.toArray(Text[]::new));
            pages++;
        }
    }

    public static void generatePlayerPackPolicy(DynamicData data, BookPage bookPage) {
        boolean policy = HostOptions.getGlobalAcceptValue(data.player());

        String cmd = "custommaploader playerOptions optionalPacks globalAccept";
        MutableText enabled = toggleCheckBox(policy, "player_rp_settings", "player_rp_settings", cmd);
        bookPage.addPage(
                dashLit("Global Pack Policy"),
                Text.empty(),
                colored("Prompt Policy", Formatting.GOLD),
                colored("Use Optional Packs: ", Formatting.DARK_GRAY).append(enabled),
                Text.empty(),
                withOpenAfterCmd(colored(bracketLit("Reset All Packs"), Formatting.DARK_RED), "player_rp_settings", "custommaploader playerOptions optionalPacks reset"),
                Text.empty(),
                backButton("options"));
    }

    public static MutableText toggleCheckBox(boolean value, String page, String arg, String cmd) {
        return colored(withOpenAfterCmd(toggleCheckBox(value), page, arg, cmd + (value ? " false" : " true")), value ? Formatting.GREEN : Formatting.RED);
    }

    public static MutableText toggleCheckBox(boolean value) {
        return withHover(Text.literal(value ? "[✔]" : "[❌]"), Text.literal("Click to toggle"));
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

    public static MutableText coloredTrans(String transKey, Formatting... formatting) {
        return colored(Text.translatable(transKey), formatting);
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

    public static MutableText voteButton(Identifier map) {
        return bracketTrans("lem.mapdecider.menu.vote").styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/custommaploader voting vote " + map)));
    }

    public static MutableText backButton(String page) {
        return colored(withOpenCmd(bracketTrans("gui.back"), page), Formatting.GRAY);
    }

    public static MutableText nextButton(String transKey, int page) {
        return colored(withClickEvent(bracketTrans(transKey), ClickEvent.Action.CHANGE_PAGE, "" + page), Formatting.GRAY);
    }

    public static MutableText withHover(MutableText text, Text hover) {
        return text.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
    }

    public static MutableText withOpenCmd(MutableText text, String page) {
        return withCmd(text, "/custommaploader voting showBookPage \"" + page + "\"");
    }

    public static MutableText withOpenCmd(MutableText text, String page, String arg) {
        return withCmd(text, "/custommaploader voting showBookPage \"" + page + "\" arg \"" + arg + "\"");
    }

    public static MutableText withOpenCmd(MutableText text, String page, Text hover) {
        return withHover(withOpenCmd(text, page), hover);
    }

    public static MutableText withOpenAfterCmd(MutableText text, String page, String afterCmd) {
        return withCmd(text, "/custommaploader voting showBookPage \"" + page + "\" after " + afterCmd);
    }

    public static MutableText withOpenAfterCmd(MutableText text, String page, String arg, String afterCmd) {
        return withCmd(text, "/custommaploader voting showBookPage \"" + page + "\" arg \"" + arg + "\" after " + afterCmd);
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
