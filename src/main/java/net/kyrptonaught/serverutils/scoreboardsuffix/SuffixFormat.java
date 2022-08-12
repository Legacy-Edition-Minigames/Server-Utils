package net.kyrptonaught.serverutils.scoreboardsuffix;

import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.*;

public class SuffixFormat {

    public List<Suffix> scoreboardSuffixes = new ArrayList<>();
    public HashSet<String> scoreboardNames = Sets.newHashSet();
    String[] input;

    public void format() {
        Arrays.stream(input).forEach(s -> {
            if (s.contains("scoreboard=")) {
                ScoreboardSuffix sbSuffix = new ScoreboardSuffix(s);
                s = sbSuffix.displayText.asString();
                String scoreboardName = s.substring(s.indexOf("=") + 1);
                sbSuffix.suffix = scoreboardName;
                scoreboardSuffixes.add(sbSuffix);
                scoreboardNames.add(scoreboardName);
            } else
                scoreboardSuffixes.add(new Suffix(s));
        });
    }

    public static class Suffix {
        public String suffix;
        public MutableText displayText;

        public Suffix(String suffix) {
            this.suffix = suffix;
            try {
                displayText = Objects.requireNonNullElseGet(Text.Serializer.fromJson(suffix), () -> new LiteralText(suffix));
            } catch (JsonParseException var4) {
                //System.out.println("\"" + suffix + "\" created error: " + var4);
                displayText = new LiteralText(suffix);
            }
        }
    }

    public static class ScoreboardSuffix extends Suffix {
        public ScoreboardSuffix(String suffix) {
            super(suffix);
        }

        public void updateText(int score) {
            displayText = new LiteralText("" + score).setStyle(displayText.getStyle());
        }
    }
}
