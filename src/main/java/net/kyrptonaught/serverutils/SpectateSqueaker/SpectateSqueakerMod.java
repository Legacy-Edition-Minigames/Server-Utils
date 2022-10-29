package net.kyrptonaught.serverutils.SpectateSqueaker;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
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
    public static final HashMap<UUID, Identifier> playerSounds = new HashMap<>();

    public void onInitialize() {
        SpectateSqueakerNetworking.registerReceivePacket();
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("registerSpectateSqueak")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.argument("soundID", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                            Identifier soundID = IdentifierArgumentType.getIdentifier(context, "soundID");
                                            players.forEach(player -> playerSounds.put(player.getUuid(), soundID));

                                            return 1;
                                        }))))
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
        Identifier soundID = playerSounds.get(player.getUuid());
        SoundEvent sound = Registry.SOUND_EVENT.get(soundID);
        if (soundID != null && sound != null) {
            player.getWorld().playSoundFromEntity(null, player, sound, SoundCategory.PLAYERS, 1, 1);
        }
    }
}
