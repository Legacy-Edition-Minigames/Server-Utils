package net.kyrptonaught.serverutils.customWorldBorder.duckInterface;

public interface CustomWorldBorder {

    void setShape(double xCenter, double zCenter, double xSize, double zSize);

    void setShape(double xCenter, double zCenter, double size);


    void enableVanillaSyncing(boolean syncing);
}
