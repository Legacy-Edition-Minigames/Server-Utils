package net.kyrptonaught.serverutils.customMapLoader;

public enum MapSize {
    AUTO("auto", "Auto"),
    SMALL("small", "Small"),
    LARGE("large", "Large"),
    LARGE_PLUS("largeplus", "Large+"),
    REMASTERED("remastered", "Remastered");

    String fileName;
    String id;

    MapSize(String fileName, String id) {
        this.fileName = fileName;
        this.id = id;
    }
}
