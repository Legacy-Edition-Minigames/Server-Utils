package net.kyrptonaught.serverutils.discordBridge;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class BookGUI {

    public static void showLinkGUI(ServerPlayerEntity player, String linkID, Text text) {
        BookElementBuilder bookBuilder = BookElementBuilder.from(Items.WRITTEN_BOOK.getDefaultStack())
                .addPage(text).signed();
        new BookGui(player, bookBuilder) {
            @Override
            public void onTakeBookButton() {
                this.close();
            }
        }.open();
    }
}
