package net.kyrptonaught.serverutils.customMapLoader.voting.pages;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BookPage {
    protected final BookElementBuilder builder;

    public BookPage(String title, String author) {
        builder = new BookElementBuilder()
                .setTitle(title)
                .setAuthor(author);
    }

    public BookPage() {
        builder = null;
    }

    public BookGui build(ServerPlayerEntity player) {
        return new BookGui(player, builder);
    }

    public BookPage addPage(Text... lines) {
        builder.addPage(lines);
        return this;
    }
}
