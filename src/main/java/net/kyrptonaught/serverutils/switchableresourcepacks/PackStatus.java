package net.kyrptonaught.serverutils.switchableresourcepacks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PackStatus {
    private final HashMap<UUID, Status> packs = new HashMap<>();

    public void addPack(UUID packname, boolean tempPack) {
        packs.put(packname, new Status(tempPack, LoadingStatus.PENDING));
    }

    public void setPackLoadStatus(UUID packname, LoadingStatus status) {
        packs.get(packname).setLoadingStatus(status);
    }

    public Map<UUID, Status> getPacks() {
        return packs;
    }

    public boolean isComplete(UUID pack) {
        return packs.get(pack).getLoadingStatus() == LoadingStatus.FINISHED || packs.get(pack).getLoadingStatus() == LoadingStatus.FAILED;
    }

    public boolean didFail(UUID pack) {
        return packs.get(pack).getLoadingStatus() == LoadingStatus.FAILED;
    }

    public static class Status {
        private boolean tempPack;
        private LoadingStatus loadingStatus;

        public Status(boolean tempPack, LoadingStatus loadingStatus) {
            this.tempPack = tempPack;
            this.loadingStatus = loadingStatus;
        }

        public void setLoadingStatus(LoadingStatus loadingStatus) {
            this.loadingStatus = loadingStatus;
        }

        public LoadingStatus getLoadingStatus() {
            return loadingStatus;
        }

        public boolean isTempPack() {
            return tempPack;
        }
    }

    public enum LoadingStatus {
        PENDING,
        STARTED,
        FAILED,
        FINISHED
    }
}
