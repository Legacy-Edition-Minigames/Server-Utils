package net.kyrptonaught.serverutils.switchableresourcepacks.status;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.switchableresourcepacks.MusicPack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class MusicStatus {
    private MusicPack masterPack;
    private MusicPack playingPack;

    private long endTime = 0;
    private Identifier currentSong;

    public boolean tickMusic = false;

    private static final Identifier DELAY_SONG = Identifier.of(ServerUtilsMod.MOD_ID, "delay");

    public void setMusic(MusicPack pack) {
        if (pack == null) {
            this.masterPack = null;
            this.playingPack = null;
            this.currentSong = null;
            this.endTime = 0;
            return;
        }

        this.masterPack = pack;
        this.playingPack = new MusicPack();
        this.playingPack.songs = new LinkedHashMap<>();
        this.playingPack.play_order = pack.play_order;
        this.playingPack.delay = pack.delay;
        this.currentSong = null;
        this.endTime = 0;
    }

    public void play() {
        tickMusic = true;
    }

    public boolean isSongFinished(long currentTime) {
        if (masterPack == null)
            return false;

        return currentTime > endTime;
    }

    public void skipSong(ServerPlayerEntity player) {
        if (masterPack == null) return;

        if (currentSong != null) stopSong(player);
        playNextSong(player, System.currentTimeMillis(), false);
    }

    public void playNextSong(ServerPlayerEntity player, long currentTime, boolean respectDelay) {
        if (playingPack.songs.isEmpty()) {
            playingPack.songs.putAll(masterPack.songs);
        }

        if (respectDelay && currentSong != DELAY_SONG) {
            int delay = getDelay(player, playingPack.delay) * 1000;
            if (delay > 0) {
                setCurrentSong(DELAY_SONG, delay, currentTime);
                return;
            }
        }

        Identifier songID;
        if (playingPack.play_order == MusicPack.PLAY_ORDER.CHRONOLOGICAL) {
            songID = playingPack.songs.sequencedKeySet().getFirst();

        } else {
            int index = player.getRandom().nextInt(playingPack.songs.size());
            Iterator<Identifier> iter = playingPack.songs.keySet().iterator();
            for (int i = 0; i < index; i++) iter.next();
            songID = iter.next();
        }

        setCurrentSong(songID, playingPack.songs.remove(songID) * 1000, currentTime);
        playSong(player, songID);
    }

    private void playSong(ServerPlayerEntity player, Identifier songID) {
        RegistryEntry<SoundEvent> registryEntry = RegistryEntry.of(SoundEvent.of(songID));
        Vec3d vec3d = player.getPos();

        player.networkHandler.sendPacket(new PlaySoundS2CPacket(registryEntry, SoundCategory.MUSIC, vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1, 1, player.getRandom().nextLong()));
        System.out.println("Playing " + songID + " for " + player.getName().getString());
    }

    private void stopSong(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new StopSoundS2CPacket(currentSong, SoundCategory.MUSIC));
    }

    private void setCurrentSong(Identifier songID, long length, long current) {
        endTime = current + length;
        currentSong = songID;
    }

    private static int getDelay(ServerPlayerEntity player, String value) {
        if (value == null || value.isEmpty())
            return 0;

        if (value.contains("-")) {
            int start = Integer.parseInt(value.substring(0, value.indexOf("-")));
            int end = Integer.parseInt(value.substring(value.indexOf("-") + 1));

            return player.getRandom().nextBetween(start, end);
        }

        if (value.contains(",")) {
            String[] values = value.split(",");
            return Integer.parseInt(values[player.getRandom().nextInt(values.length)]);
        }

        return Integer.parseInt(value);
    }
}
