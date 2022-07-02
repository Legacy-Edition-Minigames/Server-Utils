package net.kyrptonaught.serverutils.panoramaViewer;

import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanoramaConfig implements AbstractConfigFile {

    public Map<String, Integer> FrameCounts = new HashMap<>(Map.of(
            "1", 1550,
            "2", 1550,
            "3", 2075,
            "4", 3125));

    public Map<String, Integer> PaddingSize = new HashMap<>(Map.of(
            "1", 8,
            "2", 4,
            "3", 2,
            "4", 1));

    List<AutoPanoramaEntry> autoEntries = new ArrayList<>();

    List<ManualPanoramaEntry> manualEntries = new ArrayList<>();

    public static class AutoPanoramaEntry {
        public String panoramaName;

        public boolean hasNightVariant;
        public Map<String, String> texts = new HashMap<>();
    }

    public static class ManualPanoramaEntry {

        public String panoramaName;

        public String text;

        public String frameCounter;

        public String padder;
    }
}
