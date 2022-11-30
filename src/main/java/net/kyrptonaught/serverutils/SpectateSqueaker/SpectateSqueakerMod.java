package net.kyrptonaught.serverutils.SpectateSqueaker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SpectateSqueakerMod extends Module {
    public static final HashMap<UUID, PlayerSound> playerSounds = new HashMap<>();

    public void onInitialize() {
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("registerSpectateSqueak")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.argument("soundID", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                                        .then(CommandManager.argument("cooldown", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                    Identifier soundID = IdentifierArgumentType.getIdentifier(context, "soundID");
                                                    int cooldown = IntegerArgumentType.getInteger(context, "cooldown");
                                                    players.forEach(player -> playerSounds.put(player.getUuid(), new PlayerSound(soundID, cooldown)));

                                                    return 1;
                                                })))))
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            playerSounds.clear();
                            return 1;
                        })
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                    players.forEach(player -> playerSounds.remove(player.getUuid()));
                                    return 1;
                                }))));
    }

    public static void playerSqueaks(ServerPlayerEntity player) {
        PlayerSound playerSound = playerSounds.get(player.getUuid());

        if (playerSound !=null && playerSound.canUse()) {
            SoundEvent sound = Registry.SOUND_EVENT.get(playerSound.getSoundID());
            if (sound != null) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.PLAYERS, 1, 1);
            }
        }
    }

    public static class PlayerSound {
        int coolDown;
        Identifier soundID;
        long lastUsed;

        public PlayerSound(Identifier soundID, int coolDown) {
            this.soundID = soundID;
            this.coolDown = coolDown;
        }

        public boolean canUse() {
            return System.currentTimeMillis() - lastUsed > coolDown;
        }

        public Identifier getSoundID() {
            lastUsed = System.currentTimeMillis();
            return soundID;
        }
    }
}
