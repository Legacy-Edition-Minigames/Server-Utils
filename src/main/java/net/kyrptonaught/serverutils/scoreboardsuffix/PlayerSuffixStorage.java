package net.kyrptonaught.serverutils.scoreboardsuffix;

import blue.endless.jankson.api.SyntaxError;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

import java.util.HashMap;

public class PlayerSuffixStorage extends PersistentState {

    public final HashMap<String, HashMap<String, String>> playerFonts = new HashMap<>();
    public String rawSuffixFormat;
    public SuffixFormat suffixFormat = new SuffixFormat();

    public PlayerSuffixStorage() {
        super();
    }

    public static PlayerSuffixStorage fromNbt(NbtCompound tag) {
        PlayerSuffixStorage cman = new PlayerSuffixStorage();
        if (tag.contains("suffixformat"))
            cman.setSuffixFormatInput(tag.getString("suffixformat"));

        NbtCompound playersNBT = (NbtCompound) tag.get("playerFonts");
        if (playersNBT != null)
            playersNBT.getKeys().forEach(playerName -> {
                NbtCompound playerFontsNBT = (NbtCompound) playersNBT.get(playerName);
                if (playerFontsNBT != null)
                    playerFontsNBT.getKeys().forEach(placeholder -> {
                        String font = playerFontsNBT.getString(placeholder);
                        cman.setFont(playerName, placeholder, font);
                    });
            });
        return cman;
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        if (rawSuffixFormat != null)
            tag.putString("suffixformat", rawSuffixFormat);
        NbtCompound playersNBT = new NbtCompound();
        playerFonts.forEach((playerName, playerFonts) -> {
            NbtCompound playerFontsNBT = new NbtCompound();
            playerFonts.forEach(playerFontsNBT::putString);
            playersNBT.put(playerName, playerFontsNBT);
        });
        tag.put("playerFonts", playersNBT);
        return tag;
    }

    public void setSuffixFormatInput(String input) {
        rawSuffixFormat = input;
        try {
            suffixFormat = ScoreboardSuffixMod.JANKSON.fromJson(rawSuffixFormat, SuffixFormat.class);
            suffixFormat.format();
        } catch (SyntaxError e) {
            e.printStackTrace();
        }
    }

    public String getFont(String playerName, String placeholder) {
        if (playerFonts.containsKey(playerName)) {
            return playerFonts.get(playerName).getOrDefault(placeholder, placeholder);
        }
        return "minecraft:default";
    }

    public void setFont(String playerName, String placeholder, String font) {
        playerFonts.putIfAbsent(playerName, new HashMap<>());
        playerFonts.get(playerName).put(placeholder, font);
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}