package net.kyrptonaught.serverutils.panoramaViewer;

import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;
import net.kyrptonaught.serverutils.scoreboardsuffix.SuffixFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanoramaConfig implements AbstractConfigFile {

    public Map<Integer, Integer> FrameCounts = Map.of(
            1, 1550,
            2, 1550,
            3, 2075,
            4, 3125);

    public Map<Integer, Integer> PaddingSize = Map.of(
            1, 8,
            2, 4,
            3, 2,
            4, 1);

    List<AutoPanoramaEntry> autoEntries = new ArrayList<>();

    public static class AutoPanoramaEntry {
        public String panoramaName;

        public boolean hasNightVariant;
        public Map<String, String> texts = new HashMap<>();
    }
}
