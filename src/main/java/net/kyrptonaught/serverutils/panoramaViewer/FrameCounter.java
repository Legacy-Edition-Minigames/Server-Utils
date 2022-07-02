package net.kyrptonaught.serverutils.panoramaViewer;

public class FrameCounter {

    public int maxFrames;

    public int frame;

    public FrameCounter(int maxFrames) {
        this.maxFrames = maxFrames;
        frame = maxFrames;
    }

    private boolean canTick = false;

    public void readyForFirstTick() {
        canTick = true;
    }

    public void tick() {
        if (!canTick || !doesTick()) return;
        frame--;
        if (frame < 0) frame = maxFrames;
        canTick = false;
    }

    public boolean doesTick() {
        return maxFrames > 0;
    }
}
