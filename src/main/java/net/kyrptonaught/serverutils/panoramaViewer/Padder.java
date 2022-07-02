package net.kyrptonaught.serverutils.panoramaViewer;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Padder {
    public int paddingSize;
    public int lastPaddedFrame = -1;

    public Text paddedText;

    public Padder(int paddingSize) {
        this.paddingSize = paddingSize;
    }

    public void updatePadding(FrameCounter frameCounter) {
        if (frameCounter.frame == lastPaddedFrame) return;
        this.paddedText = generatePadding(frameCounter.frame);
        this.lastPaddedFrame = frameCounter.frame;
    }

    public boolean doesPad() {
        return paddingSize > 0;
    }

    private MutableText generatePadding(int frame) {
        if (!doesPad())
            return (MutableText) LiteralText.EMPTY;

        String padding = smartPad(frame * paddingSize, "");
        return new LiteralText(padding);
    }

    public static String smartPad(int frames, String output) {
        //if (frames >= 1024) return smartPad(frames - 1024, output + "\uF82E");
        //if (frames >= 512) return smartPad(frames - 512, output + "\uF82D");
        if (frames >= 128) return smartPad(frames - 128, output + "\uF82C");
        if (frames >= 64) return smartPad(frames - 64, output + "\uF82B");
        if (frames >= 32) return smartPad(frames - 32, output + "\uF82A");
        if (frames >= 16) return smartPad(frames - 16, output + "\uF829");
        if (frames >= 8) return smartPad(frames - 8, output + "\uF828");
        if (frames >= 7) return output + "\uF827";
        if (frames >= 6) return output + "\uF826";
        if (frames >= 5) return output + "\uF825";
        if (frames >= 4) return output + "\uF824";
        if (frames >= 3) return output + "\uF823";
        if (frames >= 2) return output + "\uF822";
        if (frames >= 1) return output + "\uF821";

        return output;
    }

    public MutableText padOutput(Text input) {
        Style style = Style.EMPTY.withFont(input.getStyle().getFont());
        MutableText text = paddedText.copy().setStyle(style);
        return text.append(input);
    }
}
