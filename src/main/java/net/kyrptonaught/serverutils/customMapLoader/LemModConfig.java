package net.kyrptonaught.serverutils.customMapLoader;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import java.nio.file.Path;

public class LemModConfig {

    private String name;
    private String description;

    private String name_key;
    private String description_key;

    public String authors;
    public String version;
    public String id;
    public String type;
    public String pack;

    public boolean hassmall = false;
    public boolean haslarge = false;
    public boolean haslargeplus = false;
    public boolean hasremastered = false;

    public String centercoords_small;
    public String centercoords_large;
    public String centercoords_largeplus;
    public String centercoords_remastered;

    public String mappack;

    public String mappack_key;

    public Identifier dimensionType;

    public transient DimensionType loadedDimensionType;

    public transient Path filePath;

    public transient boolean isBaseMap = false;

    public transient  boolean isEnabled = true;

    public MutableText getName() {
        if (name_key != null)
            return Text.translatable(name_key);
        return Text.literal(name);
    }

    public MutableText getDescription() {
        if (description_key != null)
            return Text.translatable(description_key);
        return Text.literal(description);
    }

    public MutableText getMapPack() {
        if (mappack_key != null)
            return Text.translatable(mappack_key);
        return Text.literal(mappack);
    }

    public Vec3d getCoordsForSize(MapSize mapSize) {
        String[] coords = (switch (mapSize) {
            case AUTO -> null;
            case SMALL -> centercoords_small;
            case LARGE -> centercoords_large;
            case LARGE_PLUS -> centercoords_largeplus;
            case REMASTERED -> centercoords_remastered;
        }).split(" ");

        return new Vec3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
    }
}
