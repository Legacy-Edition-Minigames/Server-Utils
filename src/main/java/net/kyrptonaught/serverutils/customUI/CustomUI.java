package net.kyrptonaught.serverutils.customUI;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Objects;

public class CustomUI extends Module {
    public static HashMap<String, ScreenConfig> screens = new HashMap<>();

    public static void showScreenFor(String screen, ServerPlayerEntity player) {
        ScreenConfig config = screens.get(screen);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true);
        gui.setTitle(getAsText(config.title));
        for (Integer slot : config.slots.keySet()) {
            ScreenConfig.SlotDefinition slotDefinition = config.slots.get(slot);

            ItemStack itemStack = Registry.ITEM.get(new Identifier(slotDefinition.itemID)).getDefaultStack();
            if (slotDefinition.itemNBT != null)
                try {
                    NbtCompound compound = StringNbtReader.parse(slotDefinition.itemNBT);
                    itemStack.setNbt(compound);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

            if (slotDefinition.displayName != null)
                itemStack.setCustomName(getAsText(slotDefinition.displayName));

            gui.setSlot(slot, GuiElementBuilder.from(itemStack)
                    .setCallback((index, type, action) -> {
                        if (type.isLeft) handleClick(player, slotDefinition.leftClickAction);
                        if (type.isRight) handleClick(player, slotDefinition.rightClickAction);
                    })
            );
        }

        gui.open();
    }

    private static void handleClick(ServerPlayerEntity player, String action) {
        if (action == null) return;

        String cmd = action.substring(action.indexOf("/") + 1).trim();
        if (action.startsWith("command/")) {
            CMDHelper.executeAs(player, cmd);
        } else if (action.startsWith("function/")) {
            CommandFunctionManager functionManager = player.getServer().getCommandFunctionManager();
            functionManager.getFunction(new Identifier(cmd)).ifPresent(commandFunction -> functionManager.execute(commandFunction, player.getServer().getCommandSource().withLevel(2).withSilent()));
        } else if (action.startsWith("openUI/")) {
            showScreenFor(cmd, player);
        }
    }

    private static Text getAsText(String text) {
        try {
            return Objects.requireNonNullElseGet(Text.Serializer.fromJson(text), () -> Text.literal(text));
        } catch (JsonParseException var4) {
            return Text.literal(text);
        }
    }

    public static void reload() {
        screens.clear();
        screens = ScreenConfigLoader.loadAll();
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showCustomScreen")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("screenID", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            screens.keySet().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String screenID = StringArgumentType.getString(context, "screenID");
                            showScreenFor(screenID, context.getSource().getPlayer());
                            return 1;
                        })));
    }
}
