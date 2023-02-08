package net.kyrptonaught.serverutils.customUI;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.serverTranslator.ServerTranslator;
import net.kyrptonaught.serverutils.serverTranslator.TranslationStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.resource.ResourceType;
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
import java.util.Stack;
import java.util.UUID;

public class CustomUI extends Module {
    private static final HashMap<String, ScreenConfig> screens = new HashMap<>();
    private static final HashMap<String, ScreenConfig.SlotDefinition> slotPresets = new HashMap<>();

    private static final HashMap<UUID, Stack<String>> screenHistory = new HashMap<>();

    public static void showScreenFor(String screen, ServerPlayerEntity player) {
        ScreenConfig config = screens.get(screen);

        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, true) {
            @Override
            public void onClose() {
                if (screenHistory.get(player.getUuid()).size() < 2 && !config.escToClose)
                    showScreenFor(screen, player);
                else showLastScreen(player);
            }
        };
        gui.setTitle(getAsText(config.title));
        for (Integer slot : config.slots.keySet()) {
            ScreenConfig.SlotDefinition slotDefinition = getSlotDefinition(config.slots.get(slot));

            ItemStack itemStack = Registry.ITEM.get(new Identifier(slotDefinition.itemID)).getDefaultStack();
            if (slotDefinition.itemNBT != null)
                try {
                    NbtCompound compound = StringNbtReader.parse(slotDefinition.itemNBT);
                    itemStack.setNbt(compound);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

            if (slotDefinition.customModelData != null)
                try {
                    String value = ServerTranslator.translate(player, slotDefinition.customModelData);
                    int intValue = Integer.parseInt(value);
                    itemStack.getNbt().putInt("CustomModelData", intValue);
                } catch (NumberFormatException e) {
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
        if (!screenHistory.containsKey(player.getUuid())) screenHistory.put(player.getUuid(), new Stack<>());
        screenHistory.get(player.getUuid()).push(screen);
        gui.open();
    }

    private static void showLastScreen(ServerPlayerEntity player) {
        if (!screenHistory.containsKey(player.getUuid()) || screenHistory.get(player.getUuid()).size() < 2) {
            return;
        }
        screenHistory.get(player.getUuid()).pop();
        String screenID = screenHistory.get(player.getUuid()).pop();
        showScreenFor(screenID, player);
    }


    private static ScreenConfig.SlotDefinition getSlotDefinition(ScreenConfig.SlotDefinition slotDefinition) {
        if (slotDefinition.presetID != null) return slotDefinition.copyFrom(slotPresets.get(slotDefinition.presetID));
        return slotDefinition;
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
        } else if (action.startsWith("close/")) {
            player.closeHandledScreen();
        } else if (action.startsWith("back/")) {
            showLastScreen(player);
        }
    }

    private static Text getAsText(String text) {
        try {
            return Objects.requireNonNullElseGet(Text.Serializer.fromJson(text), () -> Text.literal(text));
        } catch (JsonParseException var4) {
            return Text.literal(text);
        }
    }

    public static void addScreen(String screenID, ScreenConfig screenConfig) {
        screens.put(screenID, screenConfig);
    }

    public static void addPresets(String screenID, ScreenConfig screenConfig) {
        for (String presetID : screenConfig.presets.keySet()) {
            slotPresets.put(screenID + ":" + presetID, screenConfig.presets.get(presetID));
        }
    }

    public static void reload() {
        screens.clear();
        slotPresets.clear();
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ScreenConfigLoader());
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showCustomScreen")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("screenID", StringArgumentType.greedyString())
                        .suggests((context, builder) -> {
                            screens.keySet().forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (screenHistory.containsKey(player.getUuid())) screenHistory.get(player.getUuid()).clear();

                            String screenID = StringArgumentType.getString(context, "screenID");
                            showScreenFor(screenID, player);
                            return 1;
                        })));
    }
}
