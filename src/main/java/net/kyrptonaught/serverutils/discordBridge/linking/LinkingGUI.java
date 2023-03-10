package net.kyrptonaught.serverutils.discordBridge.linking;

import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LinkingGUI {

    public static void showLinkGUI(ServerPlayerEntity player, String linkID) {
        BookElementBuilder bookBuilder = BookElementBuilder.from(Items.WRITTEN_BOOK.getDefaultStack())
                .addPage(
                        Text.literal("Linking your accounts allows you to send messages to the server using the bridge channels."),
                        Text.empty(),
                        Text.literal("All rules apply, this role may be revoked if you break the rules."),
                        Text.empty(),
                        Text.empty(),
                        Text.literal("Continue").styled(LinkingGUI::nextPageStyle)
                )
                .addPage(
                        Text.literal("§lHow to link: "),
                        Text.empty(),
                        Text.literal("1. Join the ").append(Text.literal("Legacy Edition Minigames Discord Server").styled(style -> urlStyle(style, "https://discord.gg/5q2zz3EdYf"))),
                        Text.empty(),
                        Text.literal("2. Find the ").append(Text.literal("\"Account Link\" channel").styled(style -> urlStyle(style, "https://discord.com/channels/860805393441357834/1082945943827128360"))),
                        Text.empty(),
                        Text.literal("3. Click on the link button and enter the following code: ").append(Text.literal(linkID).styled(style -> copyStyle(style, linkID)))
                ).signed();
        new BookGui(player, bookBuilder) {
            @Override
            public void onTakeBookButton() {
                this.close();
            }
        }.open();
    }


    private static Style urlStyle(Style style, String url) {
        return style.withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open")));
    }

    private static Style copyStyle(Style style, String text) {
        return style.withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy")));
    }

    private static Style nextPageStyle(Style style){
        return style.withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Next Page")));
    }
}
