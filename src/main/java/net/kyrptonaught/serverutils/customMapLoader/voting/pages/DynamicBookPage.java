package net.kyrptonaught.serverutils.customMapLoader.voting.pages;

import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;

import java.util.List;
import java.util.function.BiConsumer;

public class DynamicBookPage extends BookPage {

    private BiConsumer<List<BattleMapAddon>, DynamicBookPage> runnable;

    public DynamicBookPage(String title, String author) {
        super(title, author);
    }

    public DynamicBookPage setRunner(BiConsumer<List<BattleMapAddon>, DynamicBookPage> consumer) {
        this.runnable = consumer;
        return this;
    }

    public void update(List<BattleMapAddon> addonList) {
        builder.getOrCreateNbt().remove("pages");
        runnable.accept(addonList, this);
    }
}
