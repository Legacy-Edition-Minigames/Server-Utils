package net.kyrptonaught.serverutils.switchableresourcepacks;

import net.kyrptonaught.serverutils.AbstractConfigFile;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackConfig extends AbstractConfigFile {
    public String playerCompleteFunction;
    public String playerFailedFunction;

    public String message = "plz use me";

    public List<RPOption> packs = new ArrayList<>();

    public static class RPOption {
        public Identifier packID;
        public String url;
        public String hash;

        private String name;
        private String name_key;

        private String description;
        private String description_key;

        public MutableText getNameText() {
            if (name_key != null)
                return Text.translatable(name_key);
            return Text.literal(name);
        }

        public MutableText getDescriptionText() {
            if (description_key != null)
                return Text.translatable(description_key);
            return Text.literal(description);
        }
    }
}
