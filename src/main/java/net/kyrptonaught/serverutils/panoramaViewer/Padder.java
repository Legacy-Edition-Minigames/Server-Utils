package net.kyrptonaught.serverutils.panoramaViewer;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Padder {
    public int paddingSize;
    public int lastPaddedFrame = -1;
    FrameCounter frameCounter;

    public Text paddedText;

    public Padder(FrameCounter frameCounter, int paddingSize) {
        this.frameCounter = frameCounter;
        this.paddingSize = paddingSize;
    }

    public void updatePadding() {
        if (frameCounter.frame == lastPaddedFrame) return;
        this.paddedText = generatePadding();
        this.lastPaddedFrame = frameCounter.frame;
    }

    public boolean dosePad() {
        return paddingSize > 0;
    }

    private MutableText generatePadding() {
        if (paddingSize == 0 || !frameCounter.doesTick())
            return (MutableText) LiteralText.EMPTY;

        String padding = switch (paddingSize) {
            case 1 -> "\uF821";
            case 2 -> "\uF822";
            case 3 -> "\uF823";
            case 4 -> "\uF824";
            case 5 -> "\uF825";
            case 6 -> "\uF826";
            case 7 -> "\uF827";
            case 8 -> "\uF828";
            case 16 -> "\uF829";
            case 32 -> "\uF82A";
            case 64 -> "\uF82B";
            case 128 -> "\uF82C";
            case 512 -> "\uF82D";
            case 1024 -> "\uF82E";
            default -> "";
        };

        padding = padding.repeat(frameCounter.frame);
        //padding = smartPad(frameCounter.frame * paddingSize, "");
        //System.out.println(frameCounter.frame + " " + padding.length());
        return new LiteralText(padding);
    }

    private String smartPad(int frames, String output) {
        if (frames >= 1024) return smartPad(frames - 1024, output + "\uF82E");
        if (frames >= 512) return smartPad(frames - 512, output + "\uF82D");
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
        Style style = Style.EMPTY;
        //style.withFont(panoramaEntry.parsedText.displayText.getStyle().getFont());
        return paddedText.copy().setStyle(input.getStyle()).append(input);
    }
}
