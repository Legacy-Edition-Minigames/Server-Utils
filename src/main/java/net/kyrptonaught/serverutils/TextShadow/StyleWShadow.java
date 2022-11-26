package net.kyrptonaught.serverutils.TextShadow;

public interface StyleWShadow {

    ShadowType hasShadow();

    void setShadow(ShadowType type);

    enum ShadowType {
        FORCE,
        HIDE,
        DEFAULT
    }
}
