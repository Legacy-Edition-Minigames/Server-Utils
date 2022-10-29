package net.kyrptonaught.serverutils.scoreboardsuffix;

import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.*;

public class SuffixFormat {

    public List<Suffix> scoreboardSuffixes = new ArrayList<>();
    public HashSet<String> scoreboardNames = Sets.newHashSet();
    String[] input;

    public void format() {
        Arrays.stream(input).forEach(s -> {
            if (s.contains("scoreboard=")) {
                ScoreboardSuffix sbSuffix = new ScoreboardSuffix(s);
                s = sbSuffix.displayText.getString();
                String scoreboardName = s.substring(s.indexOf("=") + 1);
                sbSuffix.suffix = scoreboardName;
                scoreboardSuffixes.add(sbSuffix);
                scoreboardNames.add(scoreboardName);
            } else
                scoreboardSuffixes.add(new Suffix(s));
        });
    }

    public static class SuffixFormatSerializer implements JsonDeserializer<SuffixFormat> {

        @Override
        public SuffixFormat deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            SuffixFormat format = new SuffixFormat();

            JsonArray array = jsonElement.getAsJsonObject().get("input").getAsJsonArray();
            format.input = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                if (element.isJsonObject())
                    format.input[i] = element.getAsJsonObject().toString();
                else
                    format.input[i] = array.get(i).getAsString();
            }
            return format;
        }
    }

    public static class Suffix {
        public String suffix;
        public MutableText displayText;

        public Suffix(String suffix) {
            this.suffix = suffix;
            try {
                displayText = Objects.requireNonNullElseGet(Text.Serializer.fromJson(suffix), () -> Text.literal(suffix));
            } catch (JsonParseException var4) {
                //System.out.println("\"" + suffix + "\" created error: " + var4);
                displayText = Text.literal(suffix);
            }
        }
    }

    public static class ScoreboardSuffix extends Suffix {
        public ScoreboardSuffix(String suffix) {
            super(suffix);
        }

        public void updateText(int score) {
            displayText = Text.literal("" + score).setStyle(displayText.getStyle());
        }
    }
}
