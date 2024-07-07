package net.kyrptonaught.serverutils.switchableresourcepacks.status;


import net.kyrptonaught.serverutils.switchableresourcepacks.MusicPack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public class PlayerStatus {
    private final PackStatus packStatus = new PackStatus();
    private final MusicStatus musicStatus = new MusicStatus();

    public void tick(ServerPlayerEntity player, long currentTime) {
        if (musicStatus.tickMusic && musicStatus.isSongFinished(currentTime)) {
            musicStatus.playNextSong(player, currentTime, true);
        }
    }

    public void startMusic() {
        musicStatus.play();
    }

    public void skipSong(ServerPlayerEntity player) {
        musicStatus.skipSong(player);
    }

    public void stopMusic() {
        setMusic(null);
        musicStatus.tickMusic = false;
    }

    public void setMusic(MusicPack pack) {
        musicStatus.setMusic(pack);
    }

    public void addPack(UUID packname, boolean tempPack) {
        packStatus.addPack(packname, tempPack);
    }

    public Map<UUID, PackStatus.Status> getPacks() {
        return packStatus.getPacks();
    }

    public void setPackLoadStatus(UUID packname, PackStatus.LoadingStatus status) {
        packStatus.setPackLoadStatus(packname, status);
    }

    public boolean isComplete(UUID pack) {
        return packStatus.isComplete(pack);
    }

    public boolean didFail(UUID pack) {
        return packStatus.didFail(pack);
    }
}
