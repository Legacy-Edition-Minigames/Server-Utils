package net.kyrptonaught.serverutils.customMapLoader.voting.pages;

import eu.pb4.sgui.api.gui.BookGui;

import java.util.function.BiConsumer;

public class DynamicBookPage extends BookPage {
    private final String title;
    private final String author;

    private BiConsumer<DynamicData, BookPage> runnable;

    public DynamicBookPage(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public DynamicBookPage setRunner(BiConsumer<DynamicData, BookPage> consumer) {
        this.runnable = consumer;
        return this;
    }

    public BookGui build(DynamicData data) {
        BookPage newPage = new BookPage(title, author);
        runnable.accept(data, newPage);
        return newPage.build(data.player());
    }
}
