package net.kyrptonaught.serverutils.panoramaViewer;

public class FrameCounter {

    public int maxFrames;

    public int frame;

    public boolean mainFrameCounter;

    public FrameCounter(int maxFrames, boolean isMainFrameCounter) {
        this.maxFrames = maxFrames;
        frame = maxFrames;
        this.mainFrameCounter = isMainFrameCounter;
    }

    public void tick(boolean isMain) {
        if (mainFrameCounter && !isMain) return;
        if (!doesTick()) return;
        frame--;
        if (frame < 0) frame = maxFrames;
    }

    public boolean doesTick() {
        return maxFrames > 0;
    }
}
