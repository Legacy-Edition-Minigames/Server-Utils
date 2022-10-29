package net.kyrptonaught.serverutils;


public abstract class ModuleWConfig<T extends AbstractConfigFile> extends Module {
    private T config;
    private T defaultConfig;


    public void setConfig(AbstractConfigFile config) {
        if (config == null) config = defaultConfig;
        this.config = (T) config;
        onConfigLoad(this.config);
    }

    public void onConfigLoad(T config) {

    }

    public T getConfig() {
        return config;
    }

    public void saveConfig() {
        ServerUtilsMod.config.save(getMOD_ID(), getConfig());
    }

    public abstract T createDefaultConfig();

    public T getDefaultConfig() {
        if (defaultConfig == null) defaultConfig = createDefaultConfig();
        return defaultConfig;
    }
}
